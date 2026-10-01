package es.ucm.fdi.is1.apuestas.api;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.Nombres;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/**
 * Sincroniza eventos y resultados con la API de datos deportivos (fila 13 del backlog, HU-21):
 * crea los partidos que faltan, actualiza fechas, introduce resultados (lo que resuelve las apuestas),
 * suspende los aplazados y anula los cancelados.
 */
@Service
public class SincronizacionService {

    private static final Logger LOG = LoggerFactory.getLogger(SincronizacionService.class);

    /** Nombre de nuestra competición para cada código de la API. */
    private static final Map<String, String> COMPETICIONES = Map.of(
            "PD", "LaLiga",
            "CL", "Champions League",
            "PL", "Premier League",
            "SA", "Serie A",
            "BL1", "Bundesliga",
            "FL1", "Ligue 1");

    /** Calificación inicial de un equipo que llega por la API; el creador de apuestas la ajusta después. */
    static final double CALIDAD_INICIAL = 6.0;

    private final FuenteDatosDeportivos fuente;
    private final ApiProperties propiedades;
    private final CompeticionRepository competiciones;
    private final EquipoRepository equipos;
    private final EventoRepository eventos;
    private final ResolucionService resolucion;
    private final Clock reloj;

    public SincronizacionService(FuenteDatosDeportivos fuente, ApiProperties propiedades,
                                 CompeticionRepository competiciones, EquipoRepository equipos,
                                 EventoRepository eventos, ResolucionService resolucion, Clock reloj) {
        this.fuente = fuente;
        this.propiedades = propiedades;
        this.competiciones = competiciones;
        this.equipos = equipos;
        this.eventos = eventos;
        this.resolucion = resolucion;
        this.reloj = reloj;
    }

    @Transactional
    public ResumenSincronizacion sincronizar() {
        if (!propiedades.configurada()) {
            throw new IllegalStateException(
                    "Falta la clave de la API: define la variable de entorno FOOTBALL_DATA_TOKEN");
        }
        ResumenSincronizacion resumen = new ResumenSincronizacion();
        LocalDate hoy = LocalDate.now(reloj);
        for (String codigo : propiedades.competiciones()) {
            try {
                List<PartidoApi> partidos = fuente.partidos(codigo, hoy.minusDays(propiedades.diasAtras()),
                        hoy.plusDays(propiedades.diasAdelante()));
                Competicion competicion = competicion(codigo);
                for (PartidoApi partido : partidos) {
                    procesar(partido, competicion, resumen);
                }
            } catch (RestClientException e) {
                LOG.warn("No se pudo sincronizar {}: {}", codigo, e.getMessage());
                resumen.error(codigo + ": " + e.getMessage());
            }
        }
        LOG.info("Sincronización con la API: {}", resumen);
        return resumen;
    }

    private void procesar(PartidoApi partido, Competicion competicion, ResumenSincronizacion resumen) {
        if (partido.homeTeam() == null || partido.homeTeam().id() == null
                || partido.awayTeam() == null || partido.awayTeam().id() == null) {
            return; // eliminatorias con rivales aún por decidir
        }
        Equipo local = equipo(partido.homeTeam(), competicion, resumen);
        Equipo visitante = equipo(partido.awayTeam(), competicion, resumen);
        LocalDateTime fecha = LocalDateTime.ofInstant(Instant.parse(partido.utcDate()),
                ZoneId.of(propiedades.zonaHoraria()));

        Evento evento = eventos.findByIdExterno(partido.id())
                .or(() -> eventos.findByLocalAndVisitanteAndFechaHoraBetween(local, visitante,
                        fecha.minusDays(2), fecha.plusDays(2)).stream().findFirst())
                .orElse(null);
        if (evento == null) {
            evento = eventos.save(new Evento(competicion, local, visitante, fecha));
            resumen.eventoNuevo();
        } else if (!fecha.equals(evento.getFechaHora()) || evento.getIdExterno() == null) {
            evento.cambiarFecha(fecha);
            resumen.eventoActualizado();
        }
        evento.setIdExterno(partido.id());
        if (evento.getFase() == null && partido.matchday() != null) {
            evento.setFase("Jornada " + partido.matchday());
        }
        aplicarEstado(partido, evento, resumen);
    }

    private void aplicarEstado(PartidoApi partido, Evento evento, ResumenSincronizacion resumen) {
        String estado = partido.status() == null ? "" : partido.status();
        switch (estado) {
            case "FINISHED" -> resultado(partido).ifPresent(r -> {
                if (evento.getEstado() != EstadoEvento.FINALIZADO || evento.getResultado() != r) {
                    resolucion.introducirResultado(evento.getId(), r);
                    resumen.resultado();
                }
            });
            case "POSTPONED", "SUSPENDED" -> {
                if (evento.getEstado() == EstadoEvento.PROGRAMADO) {
                    resolucion.suspender(evento.getId());
                }
            }
            case "CANCELLED" -> {
                if (evento.getEstado() == EstadoEvento.PROGRAMADO || evento.getEstado() == EstadoEvento.SUSPENDIDO) {
                    resolucion.anular(evento.getId());
                }
            }
            case "SCHEDULED", "TIMED" -> {
                if (evento.getEstado() == EstadoEvento.SUSPENDIDO) {
                    resolucion.reactivar(evento.getId());
                }
            }
            default -> {
                // IN_PLAY, PAUSED…: no hay nada que hacer hasta que termine
            }
        }
    }

    private static Optional<Resultado> resultado(PartidoApi partido) {
        if (partido.score() == null || partido.score().winner() == null) {
            return Optional.empty();
        }
        return switch (partido.score().winner()) {
            case "HOME_TEAM" -> Optional.of(Resultado.LOCAL);
            case "AWAY_TEAM" -> Optional.of(Resultado.VISITANTE);
            case "DRAW" -> Optional.of(Resultado.EMPATE);
            default -> Optional.empty();
        };
    }

    private Competicion competicion(String codigo) {
        String nombre = COMPETICIONES.getOrDefault(codigo, codigo);
        return competiciones.findByNombre(nombre)
                .orElseGet(() -> competiciones.save(new Competicion(nombre, Deporte.FUTBOL)));
    }

    /**
     * Busca el equipo por su identificador de la API; si no, por un nombre equivalente
     * ("Club Atlético de Madrid" = "Atlético de Madrid"); si no existe, lo crea con su escudo.
     */
    private Equipo equipo(PartidoApi.EquipoApi datos, Competicion competicion, ResumenSincronizacion resumen) {
        Equipo equipo = equipos.findByIdExterno(datos.id()).orElseGet(() -> porNombre(datos));
        if (equipo == null) {
            equipo = equipos.save(new Equipo(nombreVisible(datos), Deporte.FUTBOL, CALIDAD_INICIAL));
            resumen.equipoNuevo();
        }
        if (equipo.getIdExterno() == null) {
            equipo.setIdExterno(datos.id());
        }
        if (equipo.getEscudoUrl() == null && datos.crest() != null && datos.crest().startsWith("https://")) {
            equipo.setEscudoUrl(datos.crest());
        }
        if (!equipo.participaEn(competicion.getId())) {
            equipo.participaEn(competicion);
        }
        return equipo;
    }

    private Equipo porNombre(PartidoApi.EquipoApi datos) {
        String clave = Nombres.clave(datos.name());
        String claveCorta = Nombres.clave(datos.shortName());
        return equipos.findByDeporte(Deporte.FUTBOL).stream()
                .filter(e -> e.getIdExterno() == null)
                .filter(e -> {
                    String nuestra = Nombres.clave(e.getNombre());
                    return nuestra.equals(clave) || nuestra.equals(claveCorta);
                })
                .findFirst()
                .orElse(null);
    }

    private static String nombreVisible(PartidoApi.EquipoApi datos) {
        if (datos.name() != null && !datos.name().isBlank()) {
            return datos.name();
        }
        return datos.shortName() == null ? "Equipo " + datos.id() : datos.shortName();
    }
}

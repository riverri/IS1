package es.ucm.fdi.is1.apuestas.api;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.JugadorRepository;
import es.ucm.fdi.is1.apuestas.equipos.Nombres;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;

/**
 * Descarga las plantillas reales de los equipos que ya tenemos (LaLiga, Champions…) desde la API.
 * Una petición por competición. Crea los jugadores nuevos, actualiza los datos de los que ya estaban
 * (sin tocar su nota, que es del creador) y quita los que han dejado el equipo. Los jugadores dados de
 * alta a mano no se tocan.
 */
@Service
public class SincronizacionPlantillas {

    private static final Logger LOG = LoggerFactory.getLogger(SincronizacionPlantillas.class);

    private final FuenteDatosDeportivos fuente;
    private final ApiProperties propiedades;
    private final EquipoRepository equipos;
    private final JugadorRepository jugadores;

    public SincronizacionPlantillas(FuenteDatosDeportivos fuente, ApiProperties propiedades, EquipoRepository equipos,
                                    JugadorRepository jugadores) {
        this.fuente = fuente;
        this.propiedades = propiedades;
        this.equipos = equipos;
        this.jugadores = jugadores;
    }

    /** Resumen para mostrar al creador. */
    public record Resumen(int equipos, int nuevos, int actualizados, int retirados, List<String> errores) {

        @Override
        public String toString() {
            return equipos + " equipos, " + nuevos + " jugadores nuevos, " + actualizados + " actualizados y "
                    + retirados + " que ya no están";
        }
    }

    @Transactional
    public Resumen sincronizar() {
        if (!propiedades.configurada()) {
            throw new IllegalStateException(
                    "Falta la clave de la API: define la variable de entorno FOOTBALL_DATA_TOKEN");
        }
        Set<Long> procesados = new HashSet<>();
        int[] cuenta = new int[3];
        List<String> errores = new ArrayList<>();
        for (String codigo : propiedades.competiciones()) {
            try {
                for (PlantillaApi datos : fuente.plantillas(codigo)) {
                    Equipo equipo = buscar(datos);
                    if (equipo == null || datos.squad() == null || !procesados.add(equipo.getId())) {
                        continue;
                    }
                    actualizar(equipo, datos.squad(), cuenta);
                }
            } catch (RestClientException e) {
                LOG.warn("No se pudieron descargar las plantillas de {}: {}", codigo, e.getMessage());
                errores.add(codigo + ": " + e.getMessage());
            }
        }
        Resumen resumen = new Resumen(procesados.size(), cuenta[0], cuenta[1], cuenta[2], errores);
        LOG.info("Plantillas: {}", resumen);
        return resumen;
    }

    /** cuenta: [nuevos, actualizados, retirados]. */
    private void actualizar(Equipo equipo, List<PlantillaApi.JugadorApi> plantilla, int[] cuenta) {
        Set<Integer> enLaPlantilla = new HashSet<>();
        for (PlantillaApi.JugadorApi datos : plantilla) {
            Posicion posicion = Posicion.desdeApi(datos.position());
            if (datos.id() == null || datos.name() == null || posicion == null) {
                continue; // entrenadores y jugadores sin posición conocida
            }
            enLaPlantilla.add(datos.id());
            Jugador jugador = jugadores.findByIdExterno(datos.id()).orElse(null);
            if (jugador == null) {
                jugador = new Jugador(equipo, datos.name(), posicion);
                jugador.setIdExterno(datos.id());
                cuenta[0]++;
            } else {
                cuenta[1]++;
            }
            jugador.actualizar(equipo, datos.name(), posicion, datos.shirtNumber(), datos.nationality(),
                    fecha(datos.dateOfBirth()));
            jugadores.save(jugador);
        }
        for (Jugador jugador : jugadores.findByEquipo(equipo)) {
            if (jugador.isDeLaApi() && !enLaPlantilla.contains(jugador.getIdExterno())) {
                jugadores.delete(jugador);
                cuenta[2]++;
            }
        }
    }

    /** Por su identificador en la API o por un nombre equivalente. No crea equipos nuevos. */
    private Equipo buscar(PlantillaApi datos) {
        if (datos.id() == null) {
            return null;
        }
        String clave = Nombres.clave(datos.name());
        String claveCorta = Nombres.clave(datos.shortName());
        return equipos.findByIdExterno(datos.id()).orElseGet(() -> equipos.findByDeporte(Deporte.FUTBOL).stream()
                .filter(e -> {
                    String nuestra = Nombres.clave(e.getNombre());
                    return nuestra.equals(clave) || nuestra.equals(claveCorta);
                })
                .findFirst()
                .orElse(null));
    }

    private static LocalDate fecha(String texto) {
        if (texto == null || texto.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(texto.substring(0, 10));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

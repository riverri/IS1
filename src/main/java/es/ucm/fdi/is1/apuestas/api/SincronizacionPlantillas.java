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
 *
 * <p>Implementa HU-50. La lanzan {@link SincronizacionProgramada} (una vez al día) y el botón
 * <i>Descargar plantillas</i> de Gestión.
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

    /**
     * Descarga y aplica las plantillas de cada liga de {@link ApiProperties#ligasDePlantillas()}.
     * Si una liga falla, se apunta en los errores del resumen y se sigue con las demás.
     *
     * @return cuántos equipos se han actualizado y cuántos jugadores han entrado, cambiado o salido
     * @throws IllegalStateException si no está la clave de la API
     */
    @Transactional
    public Resumen sincronizar() {
        if (!propiedades.configurada()) {
            throw new IllegalStateException(
                    "Falta la clave de la API: define la variable de entorno FOOTBALL_DATA_TOKEN");
        }
        // Equipos ya actualizados: si un equipo llega por dos competiciones, solo cuenta la primera
        Set<Long> procesados = new HashSet<>();
        // Contadores que rellena actualizar(): [nuevos, actualizados, retirados].
        // Es un array porque Java pasa los int por copia: si actualizar() recibiera un int, sus sumas no se verían
        // aquí. Con un array, el método cambia las casillas del mismo array que tenemos nosotros
        int[] cuenta = new int[3];
        List<String> errores = new ArrayList<>();
        for (String codigo : propiedades.ligasDePlantillas()) {
            try {
                for (PlantillaApi datos : fuente.plantillas(codigo)) {
                    Equipo equipo = buscar(datos);
                    // Una plantilla vacía (la Champions en el plan gratuito) no borra la que ya tenemos.
                    // procesados.add devuelve false si el id ya estaba en el conjunto (equipo ya hecho)
                    if (equipo == null || datos.squad() == null || datos.squad().isEmpty()
                            || !procesados.add(equipo.getId())) {
                        continue;
                    }
                    actualizar(equipo, datos.squad(), cuenta);
                }
            } catch (RestClientException e) {
                // Sin conexión, límite de peticiones…: se apunta y se pasa a la liga siguiente
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
            // Por el identificador de la API: así se reconoce aunque cambie de nombre o venga de otro equipo
            Jugador jugador = jugadores.findByIdExterno(datos.id()).orElse(null);
            if (jugador == null) {
                jugador = new Jugador(equipo, datos.name(), posicion);
                jugador.setIdExterno(datos.id());
                cuenta[0]++;
            } else {
                cuenta[1]++;
            }
            // Solo los datos que da la API; la nota la pone el creador y no se toca (CA-50.1)
            jugador.actualizar(equipo, datos.name(), posicion, datos.shirtNumber(), datos.nationality(),
                    fecha(datos.dateOfBirth()));
            jugadores.save(jugador);
        }
        // Los de la API que ya no salen en la plantilla han dejado el equipo; los dados de alta a mano
        // (sin identificador de la API) se quedan (CA-50.2)
        for (Jugador jugador : jugadores.findByEquipo(equipo)) {
            if (jugador.isDeLaApi() && !enLaPlantilla.contains(jugador.getIdExterno())) {
                jugadores.delete(jugador);
                cuenta[2]++;
            }
        }
    }

    /**
     * Por su identificador en la API o por un nombre equivalente. No crea equipos nuevos.
     * El nombre se compara con {@link Nombres#clave(String)}, que quita tildes, mayúsculas y palabras como "FC".
     */
    private Equipo buscar(PlantillaApi datos) {
        if (datos.id() == null) {
            return null;
        }
        String clave = Nombres.clave(datos.name());
        String claveCorta = Nombres.clave(datos.shortName());
        return equipos.findByIdExterno(datos.id()).orElseGet(() -> equipos.findByDeporte(Deporte.FUTBOL).stream()
                // Un equipo ya enlazado con otro de la API no es este, aunque el nombre se parezca
                .filter(e -> e.getIdExterno() == null)
                .filter(e -> {
                    String nuestra = Nombres.clave(e.getNombre());
                    return nuestra.equals(clave) || nuestra.equals(claveCorta);
                })
                .findFirst()
                .orElse(null));
    }

    /** Fecha de nacimiento a partir de los 10 primeros caracteres (AAAA-MM-DD); null si falta o no es válida. */
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

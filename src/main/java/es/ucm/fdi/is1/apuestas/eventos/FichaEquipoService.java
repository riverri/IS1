package es.ucm.fdi.is1.apuestas.eventos;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.PlantillaService;

/**
 * Banco de estadísticas de equipos: ficha (HU-31, HU-32, HU-49) y cara a cara (HU-33).
 *
 * <p>Servicio de solo lectura. Todas las estadísticas se calculan con los eventos finalizados guardados en
 * la base de datos; la plantilla y la alineación las pide a {@link PlantillaService}. Lo usan
 * {@code InicioController} (ficha) y {@code ApuestaController} (cara a cara).
 */
@Service
public class FichaEquipoService {

    /** Partidos jugados que se listan en la ficha y en el cara a cara. */
    static final int ULTIMOS = 10;
    /** Resultados que forman la racha reciente (CA-31.1). */
    static final int RACHA = 5;
    /** Próximos partidos que se muestran en la ficha. */
    static final int PROXIMOS = 5;

    private final EquipoRepository equipos;
    private final EventoRepository eventos;
    private final PlantillaService plantillas;
    private final Clock reloj;

    public FichaEquipoService(EquipoRepository equipos, EventoRepository eventos, PlantillaService plantillas,
                              Clock reloj) {
        this.equipos = equipos;
        this.eventos = eventos;
        this.plantillas = plantillas;
        this.reloj = reloj;
    }

    /**
     * Ficha completa de un equipo o deportista (HU-31).
     *
     * @throws EquipoNoEncontradoException si no existe (404)
     */
    @Transactional(readOnly = true)
    public FichaEquipo ficha(Long equipoId) {
        Equipo equipo = equipos.findById(equipoId).orElseThrow(() -> new EquipoNoEncontradoException(equipoId));
        // Del más reciente al más antiguo
        List<Evento> jugados = eventos.partidosDe(equipo, EstadoEvento.FINALIZADO);
        // limit(n): se queda con los n primeros del stream
        List<FichaEquipo.PartidoJugado> ultimos = jugados.stream().limit(ULTIMOS)
                .map(e -> {
                    // Se compara por id porque Equipo no redefine equals()
                    boolean enCasa = e.getLocal().getId().equals(equipo.getId());
                    return new FichaEquipo.PartidoJugado(e, enCasa ? e.getVisitante() : e.getLocal(), enCasa,
                            ResultadoEquipo.de(e, equipo));
                })
                .toList();
        // El balance cuenta todos los partidos jugados, no solo los últimos
        Balance balance = Balance.de(jugados.stream().map(e -> ResultadoEquipo.de(e, equipo)).toList());
        LocalDateTime ahora = LocalDateTime.now(reloj);
        // Próximos: programados y sin empezar, del más cercano al más lejano
        List<Evento> proximos = eventos.partidosDe(equipo, EstadoEvento.PROGRAMADO).stream()
                .filter(e -> e.admiteApuestas(ahora))
                .sorted(Comparator.comparing(Evento::getFechaHora))
                .limit(PROXIMOS)
                .toList();
        // Un puesto por competición; se omiten aquellas en las que aún no ha jugado
        List<FichaEquipo.Puesto> clasificaciones = equipo.getCompeticiones().stream()
                .sorted(Comparator.comparing(Competicion::getNombre))
                .map(c -> puesto(c, equipo))
                .filter(p -> p != null)
                .toList();
        // El gráfico necesita los partidos en orden cronológico: se invierte la lista
        return new FichaEquipo(equipo, ultimos, balance, racha(jugados, equipo), clasificaciones, proximos,
                GraficoEvolucion.de(jugados.reversed(), equipo), plantillas.plantilla(equipo),
                plantillas.alineacion(equipo));
    }

    /**
     * Cara a cara de los dos rivales de un evento (HU-33): sus últimos enfrentamientos, el balance de
     * todos ellos visto desde el local y la racha de cada uno. Las listas pueden venir vacías si no hay
     * partidos previos (CA-33.2).
     */
    @Transactional(readOnly = true)
    public CaraACara caraACara(Evento evento) {
        Equipo local = evento.getLocal();
        Equipo visitante = evento.getVisitante();
        List<Evento> anteriores = eventos.enfrentamientos(local, visitante);
        return new CaraACara(evento, anteriores.stream().limit(ULTIMOS).toList(),
                Balance.de(anteriores.stream().map(e -> ResultadoEquipo.de(e, local)).toList()),
                racha(eventos.partidosDe(local, EstadoEvento.FINALIZADO), local),
                racha(eventos.partidosDe(visitante, EstadoEvento.FINALIZADO), visitante));
    }

    /** Últimos resultados, del más antiguo al más reciente (el último de la lista es el más reciente). */
    private static List<ResultadoEquipo> racha(List<Evento> jugados, Equipo equipo) {
        // jugados viene del más reciente al más antiguo: se cogen los RACHA primeros y se invierten
        List<ResultadoEquipo> recientes = jugados.stream().limit(RACHA).map(e -> ResultadoEquipo.de(e, equipo)).toList();
        return recientes.reversed();
    }

    /**
     * Clasificación de la competición con los partidos finalizados: 3 puntos por victoria y 1 por empate;
     * a igualdad de puntos, más victorias. Null si el equipo aún no ha jugado en ella.
     */
    private FichaEquipo.Puesto puesto(Competicion competicion, Equipo equipo) {
        // Resultados de cada participante, por id de equipo
        Map<Long, List<ResultadoEquipo>> porEquipo = new HashMap<>();
        Map<Long, String> nombres = new HashMap<>();
        for (Evento evento : eventos.findByCompeticionAndEstado(competicion, EstadoEvento.FINALIZADO)) {
            for (Equipo participante : List.of(evento.getLocal(), evento.getVisitante())) {
                porEquipo.computeIfAbsent(participante.getId(), id -> new ArrayList<>())
                        .add(ResultadoEquipo.de(evento, participante));
                nombres.put(participante.getId(), participante.getNombre());
            }
        }
        if (!porEquipo.containsKey(equipo.getId())) {
            return null;
        }
        // Orden de la tabla: más puntos, después más victorias y, por último, nombre (orden estable).
        // Entry es una pareja clave-valor del mapa (id del equipo, su balance). Comparator.<Entry<Long, Balance>>
        // indica a Java el tipo de los elementos, que no sabe deducir solo en una cadena tan larga
        List<Entry<Long, Balance>> tabla = porEquipo.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), Balance.de(e.getValue())))
                .sorted(Comparator.<Entry<Long, Balance>>comparingInt(e -> e.getValue().puntos()).reversed()
                        .thenComparing(Comparator.<Entry<Long, Balance>>comparingInt(e -> e.getValue().victorias())
                                .reversed())
                        .thenComparing(e -> nombres.get(e.getKey())))
                .toList();
        // La posición es el índice en la tabla más uno
        for (int i = 0; i < tabla.size(); i++) {
            if (tabla.get(i).getKey().equals(equipo.getId())) {
                return new FichaEquipo.Puesto(competicion, i + 1, tabla.size(), tabla.get(i).getValue());
            }
        }
        return null;
    }
}

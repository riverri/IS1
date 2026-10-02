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

/** Banco de estadísticas de equipos: ficha (HU-31) y cara a cara (HU-33). */
@Service
public class FichaEquipoService {

    static final int ULTIMOS = 10;
    static final int RACHA = 5;
    static final int PROXIMOS = 5;

    private final EquipoRepository equipos;
    private final EventoRepository eventos;
    private final Clock reloj;

    public FichaEquipoService(EquipoRepository equipos, EventoRepository eventos, Clock reloj) {
        this.equipos = equipos;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public FichaEquipo ficha(Long equipoId) {
        Equipo equipo = equipos.findById(equipoId).orElseThrow(() -> new EquipoNoEncontradoException(equipoId));
        List<Evento> jugados = eventos.partidosDe(equipo, EstadoEvento.FINALIZADO);
        List<FichaEquipo.PartidoJugado> ultimos = jugados.stream().limit(ULTIMOS)
                .map(e -> {
                    boolean enCasa = e.getLocal().getId().equals(equipo.getId());
                    return new FichaEquipo.PartidoJugado(e, enCasa ? e.getVisitante() : e.getLocal(), enCasa,
                            ResultadoEquipo.de(e, equipo));
                })
                .toList();
        Balance balance = Balance.de(jugados.stream().map(e -> ResultadoEquipo.de(e, equipo)).toList());
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<Evento> proximos = eventos.partidosDe(equipo, EstadoEvento.PROGRAMADO).stream()
                .filter(e -> e.admiteApuestas(ahora))
                .sorted(Comparator.comparing(Evento::getFechaHora))
                .limit(PROXIMOS)
                .toList();
        List<FichaEquipo.Puesto> clasificaciones = equipo.getCompeticiones().stream()
                .sorted(Comparator.comparing(Competicion::getNombre))
                .map(c -> puesto(c, equipo))
                .filter(p -> p != null)
                .toList();
        return new FichaEquipo(equipo, ultimos, balance, racha(jugados, equipo), clasificaciones, proximos);
    }

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
        List<ResultadoEquipo> recientes = jugados.stream().limit(RACHA).map(e -> ResultadoEquipo.de(e, equipo)).toList();
        return recientes.reversed();
    }

    /**
     * Clasificación de la competición con los partidos finalizados: 3 puntos por victoria y 1 por empate;
     * a igualdad de puntos, más victorias. Null si el equipo aún no ha jugado en ella.
     */
    private FichaEquipo.Puesto puesto(Competicion competicion, Equipo equipo) {
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
        List<Entry<Long, Balance>> tabla = porEquipo.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), Balance.de(e.getValue())))
                .sorted(Comparator.<Entry<Long, Balance>>comparingInt(e -> e.getValue().puntos()).reversed()
                        .thenComparing(Comparator.<Entry<Long, Balance>>comparingInt(e -> e.getValue().victorias())
                                .reversed())
                        .thenComparing(e -> nombres.get(e.getKey())))
                .toList();
        for (int i = 0; i < tabla.size(); i++) {
            if (tabla.get(i).getKey().equals(equipo.getId())) {
                return new FichaEquipo.Puesto(competicion, i + 1, tabla.size(), tabla.get(i).getValue());
            }
        }
        return null;
    }
}

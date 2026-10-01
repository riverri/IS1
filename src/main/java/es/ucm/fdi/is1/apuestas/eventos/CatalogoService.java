package es.ucm.fdi.is1.apuestas.eventos;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;

@Service
public class CatalogoService {

    private final EventoRepository eventos;
    private final Clock reloj;

    public CatalogoService(EventoRepository eventos, Clock reloj) {
        this.eventos = eventos;
        this.reloj = reloj;
    }

    /**
     * Eventos disponibles para apostar, agrupados por deporte y después por competición,
     * ordenados por fecha dentro de cada grupo (HU-19).
     */
    @Transactional(readOnly = true)
    public Map<Deporte, Map<String, List<Evento>>> eventosDisponiblesPorDeporte() {
        return eventos.findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento.PROGRAMADO, LocalDateTime.now(reloj))
                .stream()
                .collect(Collectors.groupingBy(Evento::getDeporte,
                        () -> new EnumMap<>(Deporte.class),
                        Collectors.groupingBy(e -> e.getCompeticion().getNombre(),
                                LinkedHashMap::new,
                                Collectors.toList())));
    }

    /** Los próximos partidos disponibles, para la portada. */
    @Transactional(readOnly = true)
    public List<Evento> proximos(int cuantos) {
        return eventos.findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento.PROGRAMADO, LocalDateTime.now(reloj))
                .stream()
                .limit(cuantos)
                .toList();
    }

    @Transactional(readOnly = true)
    public Evento eventoDisponible(Long id) {
        return eventos.findById(id)
                .filter(e -> e.admiteApuestas(LocalDateTime.now(reloj)))
                .orElseThrow(() -> new EventoNoDisponibleException(id));
    }
}

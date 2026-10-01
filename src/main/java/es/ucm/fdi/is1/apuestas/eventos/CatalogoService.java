package es.ucm.fdi.is1.apuestas.eventos;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
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

    /** Eventos disponibles para apostar, agrupados por deporte y ordenados por fecha (HU-19). */
    @Transactional(readOnly = true)
    public Map<Deporte, List<Evento>> eventosDisponiblesPorDeporte() {
        return eventos.findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento.PROGRAMADO, LocalDateTime.now(reloj))
                .stream()
                .collect(Collectors.groupingBy(Evento::getDeporte,
                        () -> new EnumMap<>(Deporte.class),
                        Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public Evento eventoDisponible(Long id) {
        return eventos.findById(id)
                .filter(e -> e.admiteApuestas(LocalDateTime.now(reloj)))
                .orElseThrow(() -> new EventoNoDisponibleException(id));
    }
}

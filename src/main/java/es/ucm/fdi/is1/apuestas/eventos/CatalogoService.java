package es.ucm.fdi.is1.apuestas.eventos;

import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
        return buscar(null, null);
    }

    /**
     * Búsqueda y filtros del catálogo (HU-22): por deporte y por texto en el nombre de los equipos
     * o de la competición. Sin distinguir mayúsculas ni tildes.
     */
    @Transactional(readOnly = true)
    public Map<Deporte, Map<String, List<Evento>>> buscar(Deporte deporte, String texto) {
        String buscado = normalizar(texto);
        return disponibles().stream()
                .filter(e -> deporte == null || e.getDeporte() == deporte)
                .filter(e -> buscado.isEmpty() || coincide(e, buscado))
                .collect(Collectors.groupingBy(Evento::getDeporte,
                        () -> new EnumMap<>(Deporte.class),
                        Collectors.groupingBy(e -> e.getCompeticion().getNombre(),
                                LinkedHashMap::new,
                                Collectors.toList())));
    }

    /** Número de eventos disponibles de cada deporte, para las pestañas del catálogo. */
    @Transactional(readOnly = true)
    public Map<Deporte, Long> recuentoPorDeporte() {
        return disponibles().stream()
                .collect(Collectors.groupingBy(Evento::getDeporte,
                        () -> new EnumMap<>(Deporte.class),
                        Collectors.counting()));
    }

    private List<Evento> disponibles() {
        return eventos.findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento.PROGRAMADO, LocalDateTime.now(reloj));
    }

    private static boolean coincide(Evento evento, String buscado) {
        return normalizar(evento.getLocal().getNombre()).contains(buscado)
                || normalizar(evento.getVisitante().getNombre()).contains(buscado)
                || normalizar(evento.getCompeticion().getNombre()).contains(buscado);
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
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

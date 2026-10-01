package es.ucm.fdi.is1.apuestas.equipos;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Listado público de equipos y deportistas, por deporte y competición, con buscador. */
@Service
public class EquiposService {

    private final EquipoRepository equipos;
    private final CompeticionRepository competiciones;

    public EquiposService(EquipoRepository equipos, CompeticionRepository competiciones) {
        this.equipos = equipos;
        this.competiciones = competiciones;
    }

    /** Equipos agrupados por deporte y ordenados por nombre; los filtros son opcionales. */
    @Transactional(readOnly = true)
    public Map<Deporte, List<Equipo>> buscar(Deporte deporte, Long competicionId, String texto) {
        String buscado = Nombres.normalizar(texto);
        return equipos.findAll().stream()
                .filter(e -> deporte == null || e.getDeporte() == deporte)
                .filter(e -> competicionId == null || e.participaEn(competicionId))
                .filter(e -> buscado.isEmpty() || Nombres.normalizar(e.getNombre()).contains(buscado))
                .sorted(Comparator.comparing(e -> Nombres.normalizar(e.getNombre())))
                .collect(Collectors.groupingBy(Equipo::getDeporte, () -> new EnumMap<>(Deporte.class),
                        Collectors.toList()));
    }

    /** Número de equipos de cada deporte, para las pestañas. */
    @Transactional(readOnly = true)
    public Map<Deporte, Long> recuentoPorDeporte() {
        return equipos.findAll().stream()
                .collect(Collectors.groupingBy(Equipo::getDeporte, () -> new EnumMap<>(Deporte.class),
                        Collectors.counting()));
    }

    /** Competiciones de un deporte, para filtrar dentro de la pestaña. */
    @Transactional(readOnly = true)
    public List<Competicion> competiciones(Deporte deporte) {
        return competiciones.findAllByOrderByDeporteAscNombreAsc().stream()
                .filter(c -> c.getDeporte() == deporte)
                .toList();
    }
}

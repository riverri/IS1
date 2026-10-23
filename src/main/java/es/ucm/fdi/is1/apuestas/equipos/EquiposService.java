package es.ucm.fdi.is1.apuestas.equipos;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listado público de equipos y deportistas, por deporte y competición, con buscador.
 * Servicio de solo lectura que usa {@code InicioController} en {@code GET /equipos}; desde cada equipo
 * se entra en su ficha (HU-31).
 */
@Service
public class EquiposService {

    private final EquipoRepository equipos;
    private final CompeticionRepository competiciones;

    public EquiposService(EquipoRepository equipos, CompeticionRepository competiciones) {
        this.equipos = equipos;
        this.competiciones = competiciones;
    }

    /**
     * Equipos agrupados por deporte y ordenados por nombre; los filtros son opcionales.
     *
     * @param deporte       null para todos los deportes
     * @param competicionId null para todas las competiciones
     * @param texto         parte del nombre, sin distinguir mayúsculas ni tildes; null o vacío para no filtrar
     */
    @Transactional(readOnly = true)
    public Map<Deporte, List<Equipo>> buscar(Deporte deporte, Long competicionId, String texto) {
        String buscado = Nombres.normalizar(texto);
        // Cada filter deja pasar solo los equipos que cumplen la condición. Las condiciones "x == null || ..."
        // significan "si no hay filtro, pasan todos"
        return equipos.findAll().stream()
                .filter(e -> deporte == null || e.getDeporte() == deporte)
                .filter(e -> competicionId == null || e.participaEn(competicionId))
                .filter(e -> buscado.isEmpty() || Nombres.normalizar(e.getNombre()).contains(buscado))
                // Por el nombre normalizado, para que una inicial con tilde ("Á") no quede detrás de la "Z"
                .sorted(Comparator.comparing(e -> Nombres.normalizar(e.getNombre())))
                // EnumMap: los deportes salen en el orden del enum
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

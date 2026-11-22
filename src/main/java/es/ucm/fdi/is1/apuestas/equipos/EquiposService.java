package es.ucm.fdi.is1.apuestas.equipos;

import java.util.EnumMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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

    private static final int TAMANO_PAGINA = 50;

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
        List<Equipo> candidatos = competicionId != null
                ? (deporte == null ? equipos.deCompeticion(competicionId)
                        : equipos.deDeporteYCompeticion(deporte, competicionId))
                : (deporte == null ? equipos.findAll() : equipos.findByDeporte(deporte));
        return candidatos.stream()
                .filter(e -> buscado.isEmpty() || Nombres.normalizar(e.getNombre()).contains(buscado))
                // Por el nombre normalizado, para que una inicial con tilde ("Á") no quede detrás de la "Z"
                .sorted(Comparator.comparing(e -> Nombres.normalizar(e.getNombre())))
                // EnumMap: los deportes salen en el orden del enum
                .collect(Collectors.groupingBy(Equipo::getDeporte, () -> new EnumMap<>(Deporte.class),
                        Collectors.toList()));
    }

    /** Página acotada del listado público, con filtros y orden ejecutados en la base de datos. */
    @Transactional(readOnly = true)
    public PaginaEquipos buscar(Deporte deporte, Long competicionId, String texto, int numero) {
        int pagina = Math.min(Math.max(0, numero), 10000);
        Slice<Equipo> resultado = equipos.paginaListado(deporte, competicionId, Nombres.normalizar(texto),
                PageRequest.of(pagina, TAMANO_PAGINA));
        List<Equipo> visibles = resultado.getContent();
        if (!visibles.isEmpty()) {
            // La tabla muestra las competiciones: carga la colección solo de estos 50 equipos, sin paginar un join.
            equipos.competicionesDe(visibles.stream().map(Equipo::getId).toList());
        }
        Map<Deporte, List<Equipo>> agrupados = visibles.stream()
                .collect(Collectors.groupingBy(Equipo::getDeporte, () -> new EnumMap<>(Deporte.class),
                        Collectors.toList()));
        return new PaginaEquipos(agrupados, pagina, pagina > 0, resultado.hasNext());
    }

    public record PaginaEquipos(Map<Deporte, List<Equipo>> equipos, int numero,
                                boolean anterior, boolean siguiente) {
    }

    /** Número de equipos de cada deporte, para las pestañas. */
    @Transactional(readOnly = true)
    public Map<Deporte, Long> recuentoPorDeporte() {
        Map<Deporte, Long> recuento = new EnumMap<>(Deporte.class);
        for (Object[] fila : equipos.contarPorDeporte()) {
            recuento.put((Deporte) fila[0], (Long) fila[1]);
        }
        return recuento;
    }

    /** Competiciones de un deporte, para filtrar dentro de la pestaña. */
    @Transactional(readOnly = true)
    public List<Competicion> competiciones(Deporte deporte) {
        return competiciones.findByDeporteOrderByNombreAsc(deporte);
    }
}

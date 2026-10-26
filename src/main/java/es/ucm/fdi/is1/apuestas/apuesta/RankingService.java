package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Ranking de jugadores por saldo, ganancias o porcentaje de aciertos (HU-36). También da el perfil público
 * (HU-48) y el ranking de una liga privada (HU-51, desde {@code LigaService}). Solo aparecen los usuarios con
 * rol de jugador y sin cuenta eliminada. Se calcula en cada consulta, así siempre está al día (CA-36.3).
 */
@Service
public class RankingService {

    /** Apuestas resueltas necesarias para competir por el porcentaje de aciertos. */
    public static final int MINIMO_PARA_ACIERTOS = 5;

    private final UsuarioRepository usuarios;
    private final ApuestaRepository apuestas;

    public RankingService(UsuarioRepository usuarios, ApuestaRepository apuestas) {
        this.usuarios = usuarios;
        this.apuestas = apuestas;
    }

    /** Ranking de todos los jugadores; {@code emailActual} (null sin sesión) marca la fila propia. */
    @Transactional(readOnly = true)
    public List<PuestoRanking> ranking(String emailActual, CriterioRanking criterio) {
        return ranking(emailActual, criterio, null);
    }

    /** Ranking solo entre algunos jugadores, como los miembros de una liga privada (HU-51). Null = todos. */
    @Transactional(readOnly = true)
    public List<PuestoRanking> ranking(String emailActual, CriterioRanking criterio, Set<Long> soloUsuarios) {
        // Los emails se guardan en minúsculas: se normaliza para compararlo
        String yo = emailActual == null ? null : emailActual.trim().toLowerCase(Locale.ROOT);
        // Una sola consulta agregada para todos: fila = [id, resueltas, ganadas, apostado, pagado]
        // HashMap: diccionario id del usuario → sus estadísticas, para encontrarlas al instante después
        Map<Long, Estadisticas> porUsuario = new HashMap<>();
        for (Object[] fila : apuestas.resumenResueltasPorUsuario()) {
            // El tipo exacto de count y sum (Long, Integer…) depende de la consulta: se leen como Number.
            // (Long) fila[0] es un "cast": le dice a Java de qué tipo es realmente ese Object
            porUsuario.put((Long) fila[0], Estadisticas.de(((Number) fila[1]).longValue(),
                    ((Number) fila[2]).longValue(), (BigDecimal) fila[3], (BigDecimal) fila[4]));
        }

        // Record local: un tipo auxiliar que solo existe dentro de este método, para juntar usuario y estadísticas
        record Fila(Usuario usuario, Estadisticas estadisticas) {
        }
        // Los jugadores sin apuestas resueltas también salen, con estadísticas vacías (getOrDefault)
        List<Fila> filas = new ArrayList<>(usuarios.findByRolOrderBySaldoDescNombreAsc(Rol.USUARIO).stream()
                .filter(u -> !u.isEliminado())
                .filter(u -> soloUsuarios == null || soloUsuarios.contains(u.getId()))
                .map(u -> new Fila(u, porUsuario.getOrDefault(u.getId(), Estadisticas.VACIAS)))
                .toList());

        // Un Comparator dice cómo ordenar dos filas. comparing(f -> valor) ordena por ese valor de menor a mayor,
        // reversed() le da la vuelta (de mayor a menor) y thenComparing desempata con otro criterio
        Comparator<Fila> orden = switch (criterio) {
            case SALDO -> Comparator.comparing((Fila f) -> f.usuario().getSaldo()).reversed();
            case GANANCIAS -> Comparator.comparing((Fila f) -> f.estadisticas().beneficio()).reversed();
            // Primero los que tienen suficientes apuestas resueltas: acertar 1 de 1 no es un 100 % comparable
            case ACIERTOS -> Comparator.comparing((Fila f) -> f.estadisticas().resueltas() >= MINIMO_PARA_ACIERTOS)
                    .reversed()
                    .thenComparing(Comparator.comparing((Fila f) -> f.estadisticas().isTieneDatos()).reversed())
                    .thenComparing(Comparator.comparing((Fila f) -> f.estadisticas().getPorcentajeAciertos()).reversed());
        };
        // A igualdad, por nombre, para que el orden sea siempre el mismo
        filas.sort(orden.thenComparing(f -> f.usuario().getNombre()));

        // La posición es el índice + 1: no hay puestos compartidos aunque haya empates
        List<PuestoRanking> puestos = new ArrayList<>();
        for (int i = 0; i < filas.size(); i++) {
            Fila f = filas.get(i);
            puestos.add(new PuestoRanking(i + 1, f.usuario().getId(), f.usuario().getNombre(), f.usuario().getSaldo(),
                    f.estadisticas(), f.usuario().getEmail().equals(yo)));
        }
        return puestos;
    }

    /**
     * Perfil público de un jugador (HU-48): su puesto en el ranking por saldo y sus estadísticas,
     * sin sus apuestas. El creador de apuestas no juega y no tiene perfil.
     *
     * @throws JugadorNoEncontradoException si no es un jugador del ranking
     */
    @Transactional(readOnly = true)
    public PerfilJugador perfil(Long usuarioId, String emailActual) {
        List<PuestoRanking> porSaldo = ranking(emailActual, CriterioRanking.SALDO);
        PuestoRanking puesto = porSaldo.stream().filter(p -> p.usuarioId().equals(usuarioId)).findFirst()
                .orElseThrow(() -> new JugadorNoEncontradoException(usuarioId));
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow();
        long enJuego = apuestas.countByUsuarioAndEstado(usuario, EstadoApuesta.ACTIVA);
        return new PerfilJugador(puesto, porSaldo.size(), enJuego);
    }
}

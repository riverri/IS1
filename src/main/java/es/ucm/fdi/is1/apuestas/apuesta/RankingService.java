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

/** Ranking de jugadores por saldo, ganancias o porcentaje de aciertos (HU-36). */
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

    @Transactional(readOnly = true)
    public List<PuestoRanking> ranking(String emailActual, CriterioRanking criterio) {
        return ranking(emailActual, criterio, null);
    }

    /** Ranking solo entre algunos jugadores, como los miembros de una liga privada (HU-51). Null = todos. */
    @Transactional(readOnly = true)
    public List<PuestoRanking> ranking(String emailActual, CriterioRanking criterio, Set<Long> soloUsuarios) {
        String yo = emailActual == null ? null : emailActual.trim().toLowerCase(Locale.ROOT);
        Map<Long, Estadisticas> porUsuario = new HashMap<>();
        for (Object[] fila : apuestas.resumenResueltasPorUsuario()) {
            porUsuario.put((Long) fila[0], Estadisticas.de(((Number) fila[1]).longValue(),
                    ((Number) fila[2]).longValue(), (BigDecimal) fila[3], (BigDecimal) fila[4]));
        }

        record Fila(Usuario usuario, Estadisticas estadisticas) {
        }
        List<Fila> filas = new ArrayList<>(usuarios.findByRolOrderBySaldoDescNombreAsc(Rol.USUARIO).stream()
                .filter(u -> !u.isEliminado())
                .filter(u -> soloUsuarios == null || soloUsuarios.contains(u.getId()))
                .map(u -> new Fila(u, porUsuario.getOrDefault(u.getId(), Estadisticas.VACIAS)))
                .toList());

        Comparator<Fila> orden = switch (criterio) {
            case SALDO -> Comparator.comparing((Fila f) -> f.usuario().getSaldo()).reversed();
            case GANANCIAS -> Comparator.comparing((Fila f) -> f.estadisticas().beneficio()).reversed();
            // Primero los que tienen suficientes apuestas resueltas: acertar 1 de 1 no es un 100 % comparable
            case ACIERTOS -> Comparator.comparing((Fila f) -> f.estadisticas().resueltas() >= MINIMO_PARA_ACIERTOS)
                    .reversed()
                    .thenComparing(Comparator.comparing((Fila f) -> f.estadisticas().isTieneDatos()).reversed())
                    .thenComparing(Comparator.comparing((Fila f) -> f.estadisticas().getPorcentajeAciertos()).reversed());
        };
        filas.sort(orden.thenComparing(f -> f.usuario().getNombre()));

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

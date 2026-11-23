package es.ucm.fdi.is1.apuestas.apuesta;

import java.util.ArrayList;
import java.util.Comparator;
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

    /** Datos de la portada del ranking: veinte puestos y, si hace falta, la fila del usuario conectado. */
    public record ListaRanking(List<PuestoRanking> primeros, PuestoRanking mio, long total) {
    }

    /** Apuestas resueltas necesarias para competir por el porcentaje de aciertos. */
    public static final int MINIMO_PARA_ACIERTOS = 5;

    private final UsuarioRepository usuarios;
    private final ApuestaRepository apuestas;
    private final EstadisticasPorJuego porJuego;

    public RankingService(UsuarioRepository usuarios, ApuestaRepository apuestas, EstadisticasPorJuego porJuego) {
        this.usuarios = usuarios;
        this.apuestas = apuestas;
        this.porJuego = porJuego;
    }

    /** Ranking de todos los jugadores; {@code emailActual} (null sin sesión) marca la fila propia. */
    @Transactional(readOnly = true)
    public List<PuestoRanking> ranking(String emailActual, CriterioRanking criterio) {
        return ranking(emailActual, criterio, null);
    }

    /** Ranking solo entre algunos jugadores, como los miembros de una liga privada (HU-51). Null = todos. */
    @Transactional(readOnly = true)
    public List<PuestoRanking> ranking(String emailActual, CriterioRanking criterio, Set<Long> soloUsuarios) {
        return ranking(emailActual, criterio, soloUsuarios, null);
    }

    /**
     * Ranking de un tipo de juego (HU-84): solo los que han jugado a ese tipo, con sus estadísticas de ese tipo
     * (CA-84.1). No se ordena por saldo, que es común a todos los juegos, ni por aciertos en cripto (CA-84.2): esos
     * criterios pasan a ganancias. Con {@code juego} null es el ranking general.
     */
    @Transactional(readOnly = true)
    public List<PuestoRanking> ranking(String emailActual, CriterioRanking criterio, Set<Long> soloUsuarios,
                                       TipoJuego juego) {
        if (juego != null && !juego.admite(criterio)) {
            criterio = CriterioRanking.GANANCIAS;
        }
        // Los emails se guardan en minúsculas: se normaliza para compararlo
        String yo = emailActual == null ? null : emailActual.trim().toLowerCase(Locale.ROOT);
        // Diccionario id del usuario → sus estadísticas, para encontrarlas al instante después
        // En una liga (soloUsuarios) basta con sus miembros: ni todas las cuentas ni las apuestas de todos
        Map<Long, Estadisticas> porUsuario = soloUsuarios != null && juego == null
                ? porJuego.deportesDe(soloUsuarios) : porJuego.de(juego);

        // Record local: un tipo auxiliar que solo existe dentro de este método, para juntar usuario y estadísticas
        record Fila(Usuario usuario, Estadisticas estadisticas) {
        }
        // Los jugadores sin apuestas resueltas también salen, con estadísticas vacías (getOrDefault)
        List<Usuario> candidatos = soloUsuarios == null ? usuarios.findByRolOrderBySaldoDescNombreAsc(Rol.USUARIO)
                : usuarios.findAllById(soloUsuarios).stream().filter(u -> u.getRol() == Rol.USUARIO).toList();
        List<Fila> filas = new ArrayList<>(candidatos.stream()
                .filter(u -> !u.isEliminado())
                .filter(u -> soloUsuarios == null || soloUsuarios.contains(u.getId()))
                // En un tipo de juego, solo los que han jugado a él (CA-84.1)
                .filter(u -> juego == null || porUsuario.containsKey(u.getId()))
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
        // A igualdad, por nombre y, si se llaman igual, por id: el orden es siempre el mismo y el perfil (HU-48) puede
        // calcular el mismo puesto sin cargar el ranking entero
        filas.sort(orden.thenComparing(f -> f.usuario().getNombre()).thenComparing(f -> f.usuario().getId()));

        // La posición es el índice + 1: no hay puestos compartidos aunque haya empates
        List<PuestoRanking> puestos = new ArrayList<>();
        for (int i = 0; i < filas.size(); i++) {
            Fila f = filas.get(i);
            puestos.add(new PuestoRanking(i + 1, f.usuario().getId(), f.usuario().getNombre(), f.usuario().getSaldo(),
                    f.estadisticas(), f.usuario().getEmail().equals(yo)));
        }
        return puestos;
    }

    /** El ranking general se calcula en SQL y solo transporta los veinte primeros y la fila propia. */
    @Transactional(readOnly = true)
    public ListaRanking lista(String emailActual, CriterioRanking criterio, TipoJuego juego) {
        if (juego != null) {
            List<PuestoRanking> todos = ranking(emailActual, criterio, null, juego);
            return new ListaRanking(todos.stream().limit(20).toList(), todos.stream()
                    .filter(PuestoRanking::soyYo).filter(p -> p.posicion() > 20).findFirst().orElse(null),
                    todos.size());
        }
        String email = emailActual == null ? null : emailActual.trim().toLowerCase(Locale.ROOT);
        Long usuarioId = email == null ? null : usuarios.findByEmail(email).map(Usuario::getId).orElse(null);
        List<ApuestaRepository.PuestoGeneral> filas = apuestas.puestosGenerales(criterio.name(),
                MINIMO_PARA_ACIERTOS, usuarioId);
        List<PuestoRanking> primeros = new ArrayList<>();
        PuestoRanking mio = null;
        for (ApuestaRepository.PuestoGeneral fila : filas) {
            PuestoRanking puesto = new PuestoRanking(Math.toIntExact(fila.getPosicion()), fila.getUsuarioId(),
                    fila.getNombre(), fila.getSaldo(), Estadisticas.de(fila.getResueltas(), fila.getGanadas(),
                            fila.getApostado(), fila.getPagado()), fila.getUsuarioId().equals(usuarioId));
            if (puesto.posicion() <= 20) {
                primeros.add(puesto);
            } else if (puesto.soyYo()) {
                mio = puesto;
            }
        }
        long total = usuarios.countByRolAndEliminadoFalse(Rol.USUARIO);
        return new ListaRanking(primeros, mio, total);
    }

    /**
     * Perfil público de un jugador (HU-48): su puesto en el ranking por saldo y sus estadísticas,
     * sin sus apuestas. El creador de apuestas no juega y no tiene perfil.
     *
     * @throws JugadorNoEncontradoException si no es un jugador del ranking
     */
    @Transactional(readOnly = true)
    public PerfilJugador perfil(Long usuarioId, String emailActual) {
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow(() -> new JugadorNoEncontradoException(usuarioId));
        if (usuario.isEliminado() || usuario.getRol() != Rol.USUARIO) {
            throw new JugadorNoEncontradoException(usuarioId);
        }
        // El puesto sale de la misma consulta que el ranking por saldo de /ranking: los empates se desempatan por nombre
        // en la base de datos, y en PostgreSQL ese orden depende de su configuración (en algunas, «bea» va antes que
        // «Carla»). Con la misma consulta, el perfil y el ranking coinciden siempre
        long posicion = apuestas.puestosGenerales(CriterioRanking.SALDO.name(), MINIMO_PARA_ACIERTOS, usuario.getId())
                .stream().filter(fila -> fila.getUsuarioId().equals(usuario.getId()))
                .mapToLong(ApuestaRepository.PuestoGeneral::getPosicion).findFirst()
                .orElseThrow(() -> new JugadorNoEncontradoException(usuarioId));
        int totalJugadores = Math.toIntExact(usuarios.countByRolAndEliminadoFalse(Rol.USUARIO));
        Object[] resumen = apuestas.resumenResueltasDe(usuarioId).getFirst();
        Estadisticas estadisticas = Estadisticas.de(((Number) resumen[0]).longValue(),
                ((Number) resumen[1]).longValue(), (java.math.BigDecimal) resumen[2],
                (java.math.BigDecimal) resumen[3]);
        boolean soyYo = emailActual != null && usuario.getEmail().equals(emailActual.trim().toLowerCase(Locale.ROOT));
        PuestoRanking puesto = new PuestoRanking(Math.toIntExact(posicion), usuario.getId(),
                usuario.getNombre(), usuario.getSaldo(), estadisticas, soyYo);
        long enJuego = apuestas.countByUsuarioAndEstado(usuario, EstadoApuesta.ACTIVA);
        return new PerfilJugador(puesto, totalJugadores, enJuego);
    }
}

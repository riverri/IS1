package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/**
 * Repositorio de {@link Apuesta}: consultas para Mis apuestas, el ranking, el juego responsable y el panel de la casa.
 *
 * <p>Spring Data deduce la consulta del nombre del método: {@code findBy} + condiciones unidas con {@code And}
 * + {@code OrderBy...Desc} para ordenar. Por ejemplo, {@code findByUsuarioAndEstadoOrderByFechaDesc} equivale a
 * "WHERE usuario = ? AND estado = ? ORDER BY fecha DESC". {@code In} compara con una lista de valores y
 * {@code countBy} cuenta en lugar de devolver filas. Las consultas que no caben en un nombre se escriben a mano
 * con {@code @Query} en JPQL: parecido a SQL, pero con los nombres de las clases y atributos Java.
 */
public interface ApuestaRepository extends JpaRepository<Apuesta, Long> {

    /** Apuestas del usuario en un estado, de la más reciente a la más antigua (activas, HU-24). */
    List<Apuesta> findByUsuarioAndEstadoOrderByFechaDesc(Usuario usuario, EstadoApuesta estado);

    /** Igual, pero con varios estados a la vez (historial, HU-34). */
    List<Apuesta> findByUsuarioAndEstadoInOrderByFechaDesc(Usuario usuario, Collection<EstadoApuesta> estados);

    /** Activas de una página, con desempate por id para que la navegación sea estable. */
    Slice<Apuesta> findByUsuarioAndEstadoOrderByFechaDescIdDesc(Usuario usuario, EstadoApuesta estado,
                                                                Pageable pageable);

    /** Historial de una página; la consulta incluye el id para evitar filas que salten entre páginas. */
    Slice<Apuesta> findByUsuarioAndEstadoInOrderByFechaDescIdDesc(Usuario usuario,
                                                                 Collection<EstadoApuesta> estados,
                                                                 Pageable pageable);

    /** Importe activo total, sin cargar las apuestas que no se van a mostrar. */
    @Query("select coalesce(sum(a.importe), 0) from Apuesta a where a.usuario = :usuario and a.estado = :estado")
    BigDecimal importePorEstado(Usuario usuario, EstadoApuesta estado);

    /** Apuestas de todos los usuarios en esos estados (panel de la casa, HU-53). */
    List<Apuesta> findByEstadoIn(Collection<EstadoApuesta> estados);

    /** Cuántas apuestas tiene el usuario en ese estado (apuestas en juego del perfil público, HU-48). */
    long countByUsuarioAndEstado(Usuario usuario, EstadoApuesta estado);

    /**
     * Por usuario: [id, apuestas resueltas, ganadas, apostado, pagado] de las apuestas ganadas y perdidas.
     * Se calcula en la base de datos para no cargar todas las apuestas al pintar el ranking.
     * Cada Object[] es una fila con esas 5 columnas. El texto entre """ es un "text block": un String de
     * varias líneas. "case when ... then 1 else 0 end" suma 1 por cada apuesta ganada, es decir, las cuenta.
     */
    @Query("""
            select a.usuario.id,
                   sum(case when a.estado in (es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.GANADA,
                                              es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.PERDIDA) then 1 else 0 end),
                   sum(case when a.estado = es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.GANADA then 1 else 0 end),
                   sum(a.importe), sum(a.pagado)
            from Apuesta a
            where a.estado in (es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.GANADA,
                               es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.PERDIDA,
                               es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.COBRADA)
            group by a.usuario.id""")
    List<Object[]> resumenResueltasPorUsuario();

    interface PuestoGeneral {
        Long getUsuarioId();

        Long getPosicion();

        String getNombre();

        BigDecimal getSaldo();

        Long getResueltas();

        Long getGanadas();

        BigDecimal getApostado();

        BigDecimal getPagado();
    }

    /** Ranking general calculado y ordenado en SQL; solo devuelve el top 20 y la fila solicitada. */
    @Query(value = """
            with estadisticas as (
                select u.id as usuario_id, u.nombre as nombre, u.saldo as saldo,
                       coalesce(sum(case when a.estado in ('GANADA', 'PERDIDA') then 1 else 0 end), 0) as resueltas,
                       coalesce(sum(case when a.estado = 'GANADA' then 1 else 0 end), 0) as ganadas,
                       coalesce(sum(case when a.estado in ('GANADA', 'PERDIDA', 'COBRADA')
                                         then a.importe else 0 end), 0) as apostado,
                       coalesce(sum(case when a.estado in ('GANADA', 'PERDIDA', 'COBRADA')
                                         then a.pagado else 0 end), 0) as pagado
                from usuario u
                left join apuesta a on a.usuario_id = u.id
                    and a.estado in ('GANADA', 'PERDIDA', 'COBRADA')
                where u.rol = 'USUARIO' and u.eliminado = false
                group by u.id, u.nombre, u.saldo
            ), posiciones as (
                select e.*,
                       row_number() over (order by
                           case when :criterio = 'SALDO' then e.saldo end desc,
                           case when :criterio = 'GANANCIAS' then e.pagado - e.apostado end desc,
                           case when :criterio = 'ACIERTOS' and e.resueltas >= :minimo then 1 else 0 end desc,
                           case when :criterio = 'ACIERTOS' and e.apostado <> 0 then 1 else 0 end desc,
                           case when :criterio = 'ACIERTOS' then
                               case when e.resueltas = 0 then 0 else 1.0 * e.ganadas / e.resueltas end end desc,
                           e.nombre asc, e.usuario_id asc) as posicion
                from estadisticas e
            )
            select usuario_id as "usuarioId", posicion as "posicion", nombre as "nombre", saldo as "saldo",
                   resueltas as "resueltas", ganadas as "ganadas", apostado as "apostado", pagado as "pagado"
            from posiciones
            where posicion <= 20 or usuario_id = :usuarioId
            order by posicion
            """, nativeQuery = true)
    List<PuestoGeneral> puestosGenerales(@org.springframework.data.repository.query.Param("criterio") String criterio,
                                         @org.springframework.data.repository.query.Param("minimo") int minimo,
                                         @org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);

    /** Estadísticas de un único jugador para su perfil público. */
    @Query("select coalesce(sum(case when a.estado in (es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.GANADA,"
            + " es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.PERDIDA) then 1 else 0 end), 0),"
            + " coalesce(sum(case when a.estado = es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.GANADA"
            + " then 1 else 0 end), 0), coalesce(sum(a.importe), 0), coalesce(sum(a.pagado), 0)"
            + " from Apuesta a where a.usuario.id = :usuarioId and a.estado in ("
            + " es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.GANADA,"
            + " es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.PERDIDA,"
            + " es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.COBRADA)")
    List<Object[]> resumenResueltasDe(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);

    interface ResumenGrupo {
        String getNombre();

        Long getResueltas();

        Long getGanadas();

        BigDecimal getApostado();

        BigDecimal getPagado();
    }

    /** Estadísticas completas del historial agrupadas sin hidratar todas las apuestas. */
    @Query(value = """
            select case
                     when s.candidato_id is not null then 'Largo plazo'
                     when resumen_selecciones.numero_selecciones > 1 then 'Combinadas'
                     else coalesce(c.nombre, 'Otros')
                   end as nombre,
                   sum(case when a.estado in ('GANADA', 'PERDIDA') then 1 else 0 end) as resueltas,
                   sum(case when a.estado = 'GANADA' then 1 else 0 end) as ganadas,
                   sum(a.importe) as apostado,
                   sum(a.pagado) as pagado
            from apuesta a
            join (select apuesta_id, min(id) as seleccion_id, count(*) as numero_selecciones
                  from seleccion group by apuesta_id) resumen_selecciones on resumen_selecciones.apuesta_id = a.id
            join seleccion s on s.id = resumen_selecciones.seleccion_id
            left join evento e on e.id = s.evento_id
            left join competicion c on c.id = e.competicion_id
            where a.usuario_id = :usuarioId and a.estado in ('GANADA', 'PERDIDA', 'COBRADA')
            group by case
                       when s.candidato_id is not null then 'Largo plazo'
                       when resumen_selecciones.numero_selecciones > 1 then 'Combinadas'
                       else coalesce(c.nombre, 'Otros')
                     end
            order by sum(case when a.estado in ('GANADA', 'PERDIDA') then 1 else 0 end) desc, nombre
            """, nativeQuery = true)
    List<ResumenGrupo> resumenPorGrupo(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);

    /**
     * Importe apostado desde una fecha, sin contar las apuestas canceladas (HU-10). Cada apuesta cuenta con lo que
     * se apostó al hacerla; las subidas de importe se cuentan con el libro de movimientos, cuando se hacen.
     * {@code :usuario} y {@code :desde} se sustituyen por los parámetros del método con el mismo nombre;
     * {@code coalesce(sum, 0)} devuelve 0 en lugar de null si no hay ninguna apuesta.
     */
    @Query("""
            select coalesce(sum(a.importeInicial), 0) from Apuesta a where a.usuario = :usuario and a.fecha >= :desde
            and a.estado <> es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.CANCELADA""")
    BigDecimal apostadoDesde(Usuario usuario, LocalDateTime desde);
}

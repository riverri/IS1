package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

public interface ApuestaRepository extends JpaRepository<Apuesta, Long> {

    List<Apuesta> findByUsuarioAndEstadoOrderByFechaDesc(Usuario usuario, EstadoApuesta estado);

    List<Apuesta> findByUsuarioAndEstadoInOrderByFechaDesc(Usuario usuario, Collection<EstadoApuesta> estados);

    List<Apuesta> findByEstadoIn(Collection<EstadoApuesta> estados);

    long countByUsuarioAndEstado(Usuario usuario, EstadoApuesta estado);

    /**
     * Por usuario: [id, apuestas resueltas, ganadas, apostado, pagado] de las apuestas ganadas y perdidas.
     * Se calcula en la base de datos para no cargar todas las apuestas al pintar el ranking.
     */
    @Query("""
            select a.usuario.id, count(a),
                   sum(case when a.estado = es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.GANADA then 1 else 0 end),
                   sum(a.importe), sum(a.pagado)
            from Apuesta a
            where a.estado in (es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.GANADA,
                               es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.PERDIDA)
            group by a.usuario.id""")
    List<Object[]> resumenResueltasPorUsuario();

    /** Importe apostado desde una fecha, sin contar las apuestas canceladas (HU-10). */
    @Query("""
            select coalesce(sum(a.importe), 0) from Apuesta a where a.usuario = :usuario and a.fecha >= :desde
            and a.estado <> es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.CANCELADA""")
    BigDecimal apostadoDesde(Usuario usuario, LocalDateTime desde);
}

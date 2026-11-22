package es.ucm.fdi.is1.apuestas.quiniela;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio de {@link BoletoQuiniela}. */
public interface BoletoQuinielaRepository extends JpaRepository<BoletoQuiniela, Long> {

    List<BoletoQuiniela> findByQuinielaOrderByIdAsc(Quiniela quiniela);

    Optional<BoletoQuiniela> findByQuinielaAndUsuario(Quiniela quiniela, Usuario usuario);

    long countByQuiniela(Quiniela quiniela);

    /** Recuento de boletos de varias quinielas en una sola consulta para las listas. */
    @Query("select b.quiniela.id, count(b) from BoletoQuiniela b "
            + "where b.quiniela.id in :quinielaIds group by b.quiniela.id")
    List<Object[]> contarPorQuiniela(@org.springframework.data.repository.query.Param("quinielaIds")
                                     List<Long> quinielaIds);

    /** Todos los boletos de un usuario: logros (HU-88). */
    List<BoletoQuiniela> findByUsuario(Usuario usuario);

    /** Boletos de quinielas ya repartidas, con su usuario: estadísticas del ranking por tipo de juego (HU-84). */
    @Query("""
            select b from BoletoQuiniela b join fetch b.usuario
            where b.quiniela.estado = es.ucm.fdi.is1.apuestas.quiniela.EstadoQuiniela.REPARTIDA""")
    List<BoletoQuiniela> deQuinielasRepartidas();

    /** Totales del ranking sin cargar en memoria todos los boletos y sus usuarios. */
    @Query("select b.usuario.id, count(b), sum(case when b.premio > 0 then 1 else 0 end),"
            + " sum(b.pagado), sum(b.premio) from BoletoQuiniela b"
            + " where b.quiniela.estado = es.ucm.fdi.is1.apuestas.quiniela.EstadoQuiniela.REPARTIDA"
            + " and b.devuelto = false group by b.usuario.id")
    List<Object[]> resumenRepartidasPorUsuario();
}

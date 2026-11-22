package es.ucm.fdi.is1.apuestas.ligas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio Spring Data de las ligas privadas (HU-51). */
public interface LigaRepository extends JpaRepository<Liga, Long> {

    interface ClasificacionGlobalFila {
        Long getLigaId();

        String getNombre();

        Long getMiembros();

        java.math.BigDecimal getSaldo();
    }

    Optional<Liga> findByCodigo(String codigo);

    /** Para no repetir código al generar uno nuevo. */
    boolean existsByCodigo(String codigo);

    /** Solo la página Mis ligas necesita el tamaño y los nombres de sus miembros para renderizarlos. */
    @EntityGraph(attributePaths = "miembros")
    @Query("select distinct l from Liga l join l.miembros m where m = :usuario order by l.nombre")
    List<Liga> deMiembroConMiembros(@Param("usuario") Usuario usuario);

    /** Cuenta las ligas de un usuario sin hidratar entidades ni colecciones. */
    @Query("select count(l) from Liga l join l.miembros m where m = :usuario")
    long contarDeMiembro(@Param("usuario") Usuario usuario);

    /** Ids de quienes comparten alguna liga con el usuario, sin cargar ligas ni todos sus miembros como entidades. */
    @Query("select distinct companero.id from Liga l join l.miembros actual join l.miembros companero"
            + " where actual = :usuario")
    List<Long> idsCompaneros(@Param("usuario") Usuario usuario);

    /** Comprobación de pertenencia mutua con EXISTS, sin inicializar la colección de miembros de cada liga. */
    @Query("select case when count(l) > 0 then true else false end from Liga l"
            + " join l.miembros primero join l.miembros segundo"
            + " where primero = :primero and segundo = :segundo")
    boolean compartenLiga(@Param("primero") Usuario primero, @Param("segundo") Usuario segundo);

    /** Datos agregados para la clasificación global, sin hidratar las entidades de cada miembro. */
    @Query("""
            select l.id as ligaId, l.nombre as nombre, count(m.id) as miembros,
                   coalesce(sum(m.saldo), 0) as saldo
            from Liga l left join l.miembros m on m.eliminado = false
            where l.publica = true
            group by l.id, l.nombre
            """)
    List<ClasificacionGlobalFila> resumenPublicas();
}

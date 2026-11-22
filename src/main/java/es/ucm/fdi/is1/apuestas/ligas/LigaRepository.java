package es.ucm.fdi.is1.apuestas.ligas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    /**
     * Ligas en las que está el usuario (creadas por él o no), por nombre. El join recorre la tabla liga_miembro:
     * "join l.miembros m" pone una fila por cada pareja (liga, miembro), y el where se queda con las de ese usuario.
     */
    @Query("select l from Liga l join l.miembros m where m = :usuario order by l.nombre")
    List<Liga> deMiembro(Usuario usuario);

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

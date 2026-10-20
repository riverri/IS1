package es.ucm.fdi.is1.apuestas.ligas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio Spring Data de las ligas privadas (HU-51). */
public interface LigaRepository extends JpaRepository<Liga, Long> {

    Optional<Liga> findByCodigo(String codigo);

    /** Para no repetir código al generar uno nuevo. */
    boolean existsByCodigo(String codigo);

    /** Ligas en las que está el usuario (creadas por él o no), por nombre. El join recorre la tabla liga_miembro. */
    @Query("select l from Liga l join l.miembros m where m = :usuario order by l.nombre")
    List<Liga> deMiembro(Usuario usuario);
}

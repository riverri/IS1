package es.ucm.fdi.is1.apuestas.ligas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

public interface LigaRepository extends JpaRepository<Liga, Long> {

    Optional<Liga> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    @Query("select l from Liga l join l.miembros m where m = :usuario order by l.nombre")
    List<Liga> deMiembro(Usuario usuario);
}

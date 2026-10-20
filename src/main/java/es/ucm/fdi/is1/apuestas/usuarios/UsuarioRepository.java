package es.ucm.fdi.is1.apuestas.usuarios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data de {@link Usuario}. Las consultas las genera Spring a partir del nombre
 * de cada método. Los emails deben llegar ya normalizados (ver {@link UsuarioService}).
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Usuarios de un rol, de más a menos saldo y, con el mismo saldo, por nombre (ranking, HU-36). */
    List<Usuario> findByRolOrderBySaldoDescNombreAsc(Rol rol);
}

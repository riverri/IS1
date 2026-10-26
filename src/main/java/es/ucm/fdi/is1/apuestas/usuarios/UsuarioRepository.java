package es.ucm.fdi.is1.apuestas.usuarios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data de {@link Usuario}. Las consultas las genera Spring a partir del nombre
 * de cada método. Los emails deben llegar ya normalizados (ver {@link UsuarioService}).
 *
 * <p><b>Cómo funciona un repositorio:</b> es una interfaz sin código. Al heredar de
 * {@code JpaRepository<Usuario, Long>} (entidad {@code Usuario}, con id de tipo {@code Long}) ya trae
 * {@code save}, {@code findById}, {@code findAll}, {@code delete}, {@code count}... y Spring Data escribe la
 * consulta SQL de cada método nuevo leyendo su nombre: {@code findByEmail} → {@code SELECT ... WHERE email = ?}.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca por email. Optional: puede no haber ninguno, y así el compilador obliga a tenerlo en cuenta. */
    Optional<Usuario> findByEmail(String email);

    /** true si ya hay una cuenta con ese email. */
    boolean existsByEmail(String email);

    /** Usuarios de un rol, de más a menos saldo y, con el mismo saldo, por nombre (ranking, HU-36). */
    List<Usuario> findByRolOrderBySaldoDescNombreAsc(Rol rol);
}

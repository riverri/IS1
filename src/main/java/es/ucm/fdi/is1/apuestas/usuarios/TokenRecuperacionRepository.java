package es.ucm.fdi.is1.apuestas.usuarios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Repositorio Spring Data de los enlaces para recuperar la contraseña (HU-17). */
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    /** Se busca por el resumen SHA-256 del código: el código en claro no está en la base de datos. */
    Optional<TokenRecuperacion> findByHash(String hash);

    /**
     * Al pedir un enlace nuevo, los anteriores dejan de servir. Consulta JPQL escrita a mano;
     * {@code @Modifying} indica que cambia datos (un delete) en lugar de devolver filas.
     */
    @Modifying
    @Query("delete from TokenRecuperacion t where t.usuario = ?1")
    void borrarDe(Usuario usuario);
}

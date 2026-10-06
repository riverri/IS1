package es.ucm.fdi.is1.apuestas.usuarios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByHash(String hash);

    /** Al pedir un enlace nuevo, los anteriores dejan de servir. */
    @Modifying
    @Query("delete from TokenRecuperacion t where t.usuario = ?1")
    void borrarDe(Usuario usuario);
}

package es.ucm.fdi.is1.apuestas.usuarios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/** Persistence operations for email verification tokens. */
public interface TokenVerificacionCorreoRepository extends JpaRepository<TokenVerificacionCorreo, Long> {

    Optional<TokenVerificacionCorreo> findByHash(String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TokenVerificacionCorreo t where t.hash = :hash")
    Optional<TokenVerificacionCorreo> paraConfirmar(@Param("hash") String hash);

    @Modifying
    @Query("delete from TokenVerificacionCorreo t where t.usuario = :usuario")
    void borrarDe(@Param("usuario") Usuario usuario);
}

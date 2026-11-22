package es.ucm.fdi.is1.apuestas.usuarios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/** Persistence operations for pending email address changes. */
public interface CambioCorreoPendienteRepository extends JpaRepository<CambioCorreoPendiente, Long> {

    Optional<CambioCorreoPendiente> findByHash(String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CambioCorreoPendiente c where c.hash = :hash")
    Optional<CambioCorreoPendiente> paraConfirmar(@Param("hash") String hash);

    @Modifying
    @Query("delete from CambioCorreoPendiente c where c.usuario = :usuario")
    void borrarDe(@Param("usuario") Usuario usuario);
}

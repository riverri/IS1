package es.ucm.fdi.is1.apuestas.usuarios;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/** Persistence operations for email-only registration requests. */
public interface RegistroPendienteRepository extends JpaRepository<RegistroPendiente, Long> {

    Optional<RegistroPendiente> findByEmail(String email);

    Optional<RegistroPendiente> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RegistroPendiente r where r.tokenHash = :hash")
    Optional<RegistroPendiente> paraConfirmar(@Param("hash") String hash);

    @Modifying
    @Query("delete from RegistroPendiente r where r.caduca <= :ahora")
    int borrarCaducados(@Param("ahora") LocalDateTime ahora);
}

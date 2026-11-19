package es.ucm.fdi.is1.apuestas.quiniela;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/** Repositorio de {@link Quiniela}. */
public interface QuinielaRepository extends JpaRepository<Quiniela, Long> {

    List<Quiniela> findAllByOrderByCreadaDesc();

    /**
     * La quiniela para echar un boleto, bloqueando su fila y subiendo ya su versión (PESSIMISTIC_FORCE_INCREMENT).
     * Así, si a la vez se anula o se reparte, una de las dos operaciones falla o espera a la otra, en lugar de quedar
     * un boleto cobrado en una quiniela anulada al que nadie le devuelve el precio.
     */
    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query("select q from Quiniela q where q.id = :id")
    Optional<Quiniela> paraEcharBoleto(@Param("id") Long id);
}

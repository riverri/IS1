package es.ucm.fdi.is1.apuestas.mercados;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/** Repositorio Spring Data de los mercados a largo plazo (HU-44, HU-45). */
public interface MercadoRepository extends JpaRepository<Mercado, Long> {

    /**
     * El mercado para apostar en él, con su fila bloqueada para lectura (FOR SHARE): como en
     * {@code EventoRepository.paraApostar}, una apuesta y la resolución o anulación del mercado no se cruzan.
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @EntityGraph(attributePaths = "candidatos")
    @Query("select m from Mercado m where m.id = :id")
    Optional<Mercado> paraApostar(@Param("id") Long id);

    /** El mercado para resolverlo o anularlo, con su fila bloqueada (FOR UPDATE). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "candidatos")
    @Query("select m from Mercado m where m.id = :id")
    Optional<Mercado> paraCambiar(@Param("id") Long id);

    /** Todos los mercados, del que cierra antes al que cierra después. */
    @EntityGraph(attributePaths = "candidatos")
    List<Mercado> findAllByOrderByCierreAsc();

    /** Ficha de un mercado, con su lista explícita para que se pueda renderizar con open-in-view desactivado. */
    @EntityGraph(attributePaths = "candidatos")
    @Query("select m from Mercado m where m.id = :id")
    Optional<Mercado> paraMostrar(@Param("id") Long id);

    /** Lo usan los datos iniciales para no crear dos veces el mismo mercado de ejemplo. */
    Optional<Mercado> findByNombre(String nombre);
}

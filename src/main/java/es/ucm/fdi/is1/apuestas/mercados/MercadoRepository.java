package es.ucm.fdi.is1.apuestas.mercados;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de los mercados a largo plazo (HU-44, HU-45). */
public interface MercadoRepository extends JpaRepository<Mercado, Long> {

    /** Todos los mercados, del que cierra antes al que cierra después. */
    List<Mercado> findAllByOrderByCierreAsc();

    /** Lo usan los datos iniciales para no crear dos veces el mismo mercado de ejemplo. */
    Optional<Mercado> findByNombre(String nombre);
}

package es.ucm.fdi.is1.apuestas.mercados;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MercadoRepository extends JpaRepository<Mercado, Long> {

    List<Mercado> findAllByOrderByCierreAsc();

    Optional<Mercado> findByNombre(String nombre);
}

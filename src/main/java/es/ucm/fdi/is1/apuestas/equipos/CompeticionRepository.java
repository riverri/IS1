package es.ucm.fdi.is1.apuestas.equipos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompeticionRepository extends JpaRepository<Competicion, Long> {

    List<Competicion> findAllByOrderByDeporteAscNombreAsc();

    Optional<Competicion> findByNombre(String nombre);
}

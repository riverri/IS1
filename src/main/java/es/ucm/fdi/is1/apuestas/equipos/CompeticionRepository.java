package es.ucm.fdi.is1.apuestas.equipos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompeticionRepository extends JpaRepository<Competicion, Long> {

    List<Competicion> findAllByOrderByDeporteAscNombreAsc();
}

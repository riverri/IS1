package es.ucm.fdi.is1.apuestas.equipos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findAllByOrderByDeporteAscNombreAsc();
}

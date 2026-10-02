package es.ucm.fdi.is1.apuestas.equipos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    List<Jugador> findByEquipo(Equipo equipo);

    Optional<Jugador> findByIdExterno(Integer idExterno);

    long countByEquipo(Equipo equipo);
}

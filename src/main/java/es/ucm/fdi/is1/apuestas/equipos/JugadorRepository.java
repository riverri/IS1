package es.ucm.fdi.is1.apuestas.equipos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de {@link Jugador}; las consultas salen del nombre de cada método. */
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    List<Jugador> findByEquipo(Equipo equipo);

    /** Por su identificador en football-data.org, al descargar las plantillas (HU-50). */
    Optional<Jugador> findByIdExterno(Integer idExterno);

    long countByEquipo(Equipo equipo);
}

package es.ucm.fdi.is1.apuestas.equipos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de {@link Competicion}; las consultas salen del nombre de cada método. */
public interface CompeticionRepository extends JpaRepository<Competicion, Long> {

    /** Todas, agrupadas por deporte (en el orden del enum) y por nombre dentro de cada deporte. */
    List<Competicion> findAllByOrderByDeporteAscNombreAsc();

    /**
     * Por nombre. Si hubiera dos con el mismo nombre (bases de datos de antes de impedirlo), devuelve el más
     * antiguo en lugar de fallar: con un error aquí, la aplicación no llegaba a arrancar.
     */
    default Optional<Competicion> findByNombre(String nombre) {
        return findFirstByNombreOrderByIdAsc(nombre);
    }

    /** La de menor id (la más antigua) con ese nombre exacto. */
    Optional<Competicion> findFirstByNombreOrderByIdAsc(String nombre);

    /** Para no dar de alta dos competiciones con el mismo nombre escrito con otras mayúsculas. */
    boolean existsByNombreIgnoreCase(String nombre);
}

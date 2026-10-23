package es.ucm.fdi.is1.apuestas.equipos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de {@link Equipo}; las consultas salen del nombre de cada método. */
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    /** Todos, agrupados por deporte (en el orden del enum) y por nombre dentro de cada deporte. */
    List<Equipo> findAllByOrderByDeporteAscNombreAsc();

    /**
     * Por nombre. Si hubiera dos con el mismo nombre (bases de datos de antes de impedirlo), devuelve el más
     * antiguo en lugar de fallar: con un error aquí, la aplicación no llegaba a arrancar.
     * Es un método {@code default}: una interfaz puede traer métodos con código, y este solo llama a otro.
     */
    default Optional<Equipo> findByNombre(String nombre) {
        return findFirstByNombreOrderByIdAsc(nombre);
    }

    /** El de menor id (el más antiguo) con ese nombre exacto. */
    Optional<Equipo> findFirstByNombreOrderByIdAsc(String nombre);

    /** Para no dar de alta dos equipos con el mismo nombre escrito con otras mayúsculas. */
    boolean existsByNombreIgnoreCase(String nombre);

    /** Por su identificador en football-data.org, al sincronizar con la API. */
    Optional<Equipo> findByIdExterno(Integer idExterno);

    List<Equipo> findByDeporte(Deporte deporte);
}

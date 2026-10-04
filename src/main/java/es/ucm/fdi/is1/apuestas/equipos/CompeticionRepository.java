package es.ucm.fdi.is1.apuestas.equipos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompeticionRepository extends JpaRepository<Competicion, Long> {

    List<Competicion> findAllByOrderByDeporteAscNombreAsc();

    /**
     * Por nombre. Si hubiera dos con el mismo nombre (bases de datos de antes de impedirlo), devuelve el más
     * antiguo en lugar de fallar: con un error aquí, la aplicación no llegaba a arrancar.
     */
    default Optional<Competicion> findByNombre(String nombre) {
        return findFirstByNombreOrderByIdAsc(nombre);
    }

    Optional<Competicion> findFirstByNombreOrderByIdAsc(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}

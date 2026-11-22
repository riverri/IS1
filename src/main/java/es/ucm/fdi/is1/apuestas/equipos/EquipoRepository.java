package es.ucm.fdi.is1.apuestas.equipos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repositorio Spring Data de {@link Equipo}; las consultas salen del nombre de cada método. */
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    /** Página de equipos; los tres filtros y el orden normalizado se resuelven antes de cargar entidades. */
    @Query("""
            select e from Equipo e
            where (:deporte is null or e.deporte = :deporte)
              and (:competicionId is null or exists
                   (select c.id from e.competiciones c where c.id = :competicionId))
              and (:texto = '' or cast(function('translate', lower(e.nombre), 'áéíóúüñ', 'aeiouun') as string)
                   like concat('%', :texto, '%'))
            order by cast(function('translate', lower(e.nombre), 'áéíóúüñ', 'aeiouun') as string), e.id
            """)
    Slice<Equipo> paginaListado(@Param("deporte") Deporte deporte,
                                @Param("competicionId") Long competicionId,
                                @Param("texto") String texto, Pageable pageable);

    /** Todos, agrupados por deporte (en el orden del enum) y por nombre dentro de cada deporte. */
    List<Equipo> findAllByOrderByDeporteAscNombreAsc();

    /** Listado de gestión, que filtra por competición después de esta consulta y necesita la colección cargada. */
    @EntityGraph(attributePaths = "competiciones")
    List<Equipo> findAllConCompeticionesByOrderByDeporteAscNombreAsc();

    /** Ficha estadística, que muestra sus competiciones tras cerrar la transacción del servicio. */
    @EntityGraph(attributePaths = "competiciones")
    @Query("select e from Equipo e where e.id = :id")
    Optional<Equipo> buscarConCompeticiones(@Param("id") Long id);

    /** Carga las competiciones de los equipos de una página, en una sola consulta y solo para sus ids visibles. */
    @Query("select distinct e from Equipo e left join fetch e.competiciones where e.id in :ids")
    List<Equipo> competicionesDe(@Param("ids") List<Long> ids);

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

    /** Equipos que participan en una competición, filtrados en base de datos. */
    @Query("select distinct e from Equipo e join e.competiciones c where c.id = :competicionId")
    List<Equipo> deCompeticion(@Param("competicionId") Long competicionId);

    /** Equipos de un deporte y una competición, filtrados antes de hidratar sus entidades. */
    @Query("select distinct e from Equipo e join e.competiciones c where e.deporte = :deporte and c.id = :competicionId")
    List<Equipo> deDeporteYCompeticion(@Param("deporte") Deporte deporte,
                                       @Param("competicionId") Long competicionId);

    /** Recuento por deporte sin cargar equipos ni sus competiciones. */
    @Query("select e.deporte, count(e) from Equipo e group by e.deporte")
    List<Object[]> contarPorDeporte();
}

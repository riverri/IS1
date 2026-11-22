package es.ucm.fdi.is1.apuestas.equipos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Repositorio Spring Data de {@link Jugador}; las consultas salen del nombre de cada método. */
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    /** Jugadores de una página del mercado, filtrados y ordenados por precio en la base de datos. */
    @EntityGraph(attributePaths = "equipo")
    @Query("""
            select j from Jugador j join j.equipo e
            where e.deporte = es.ucm.fdi.is1.apuestas.equipos.Deporte.FUTBOL
              and (:posicion is null or j.posicion = :posicion)
              and (:clubId is null or e.id = :clubId)
            order by j.nota * 1.5 + e.calidad * 0.5 desc, j.nombre asc, j.id asc
            """)
    Slice<Jugador> paginaMercado(@org.springframework.data.repository.query.Param("posicion") Posicion posicion,
                                 @org.springframework.data.repository.query.Param("clubId") Long clubId,
                                 Pageable pageable);

    /** Solo clubes con futbolistas registrados para llenar el filtro del mercado. */
    @Query("select distinct j.equipo from Jugador j where j.equipo.deporte = "
            + "es.ucm.fdi.is1.apuestas.equipos.Deporte.FUTBOL order by j.equipo.nombre")
    List<Equipo> clubesConJugadores();

    List<Jugador> findByEquipo(Equipo equipo);

    /** Por su identificador en football-data.org, al descargar las plantillas (HU-50). */
    Optional<Jugador> findByIdExterno(Integer idExterno);

    long countByEquipo(Equipo equipo);
}

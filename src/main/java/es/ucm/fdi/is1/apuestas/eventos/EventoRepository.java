package es.ucm.fdi.is1.apuestas.eventos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento estado, LocalDateTime fecha);

    List<Evento> findAllByOrderByFechaHoraAsc();

    boolean existsByLocalAndVisitanteAndFechaHora(Equipo local, Equipo visitante, LocalDateTime fechaHora);

    Optional<Evento> findByLocalAndVisitanteAndFechaHora(Equipo local, Equipo visitante, LocalDateTime fechaHora);

    Optional<Evento> findByIdExterno(Long idExterno);

    /** Para enlazar un partido de la API con uno creado a mano: mismos equipos y fecha parecida. */
    List<Evento> findByLocalAndVisitanteAndFechaHoraBetween(Equipo local, Equipo visitante,
                                                           LocalDateTime desde, LocalDateTime hasta);

    /** Partidos de un equipo en un estado, del más reciente al más antiguo (HU-31). */
    @Query("""
            select e from Evento e where e.estado = :estado and (e.local = :equipo or e.visitante = :equipo)
            order by e.fechaHora desc""")
    List<Evento> partidosDe(Equipo equipo, EstadoEvento estado);

    /** Partidos ya jugados entre dos equipos, en cualquier campo (HU-33). */
    @Query("""
            select e from Evento e where e.estado = es.ucm.fdi.is1.apuestas.eventos.EstadoEvento.FINALIZADO
            and ((e.local = :uno and e.visitante = :otro) or (e.local = :otro and e.visitante = :uno))
            order by e.fechaHora desc""")
    List<Evento> enfrentamientos(Equipo uno, Equipo otro);

    List<Evento> findByCompeticionAndEstado(Competicion competicion, EstadoEvento estado);
}

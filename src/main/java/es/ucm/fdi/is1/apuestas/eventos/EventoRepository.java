package es.ucm.fdi.is1.apuestas.eventos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;

/**
 * Repositorio Spring Data de {@link Evento}. Spring genera la implementación: las consultas salen del
 * nombre de cada método o de su {@code @Query} (JPQL).
 * Cómo se leen los nombres: ver {@link es.ucm.fdi.is1.apuestas.apuesta.ApuestaRepository}.
 * Aquí aparecen también {@code After} (fecha posterior a), {@code Between} (entre dos valores) y
 * {@code findAllByOrderBy...} (todos, ordenados).
 */
public interface EventoRepository extends JpaRepository<Evento, Long> {

    /**
     * El evento para apostar en él, con su fila bloqueada para lectura (FOR SHARE): las apuestas pueden ir a la vez,
     * pero esperan a que termine quien lo esté anulando o resolviendo (ver {@link #paraCambiar}), y al revés. Si no,
     * una apuesta guardada justo mientras se anula se quedaría activa para siempre, con el dinero retenido.
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select e from Evento e where e.id = :id")
    Optional<Evento> paraApostar(@Param("id") Long id);

    /** El evento para cambiar su estado (resultado, anular, suspender), con su fila bloqueada (FOR UPDATE). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Evento e where e.id = :id")
    Optional<Evento> paraCambiar(@Param("id") Long id);

    /** Eventos en un estado que empiezan después de una fecha, del más próximo al más lejano (catálogo, HU-19). */
    List<Evento> findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento estado, LocalDateTime fecha);

    /** Próximos eventos paginados para no hidratar el catálogo entero en la portada. */
    List<Evento> findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento estado, LocalDateTime fecha,
                                                                  Pageable pageable);

    /** Próximo evento de un deporte, limitado en base de datos para seleccionar el destacado. */
    @Query("""
            select e from Evento e
            where e.estado = :estado and e.fechaHora > :fecha and e.competicion.deporte = :deporte
            order by e.fechaHora asc
            """)
    List<Evento> proximosPorDeporte(@Param("estado") EstadoEvento estado,
                                   @Param("deporte") Deporte deporte,
                                   @Param("fecha") LocalDateTime fecha,
                                   Pageable pageable);

    /** Recuento del catálogo sin cargar todas las entidades Evento. */
    @Query("""
            select e.competicion.deporte, count(e)
            from Evento e
            where e.estado = :estado and e.fechaHora > :fecha
            group by e.competicion.deporte
            """)
    List<Object[]> contarDisponiblesPorDeporte(@Param("estado") EstadoEvento estado,
                                               @Param("fecha") LocalDateTime fecha);

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

    /** Eventos de una competición en un estado; con FINALIZADO sirve para calcular la clasificación (HU-31). */
    List<Evento> findByCompeticionAndEstado(Competicion competicion, EstadoEvento estado);
}

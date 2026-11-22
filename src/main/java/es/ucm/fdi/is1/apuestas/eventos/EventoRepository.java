package es.ucm.fdi.is1.apuestas.eventos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    @EntityGraph(attributePaths = {"competicion", "local", "visitante"})
    List<Evento> findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento estado, LocalDateTime fecha);

    /** Eventos del deporte seleccionado: evita cargar en Java los eventos de los demás deportes. */
    @EntityGraph(attributePaths = {"competicion", "local", "visitante"})
    @Query("""
            select e from Evento e
            where e.estado = :estado and e.fechaHora > :fecha and e.competicion.deporte = :deporte
            order by e.fechaHora asc
            """)
    List<Evento> disponiblesPorDeporte(@Param("estado") EstadoEvento estado,
                                       @Param("fecha") LocalDateTime fecha,
                                       @Param("deporte") Deporte deporte);

    /** Página del catálogo con deporte y búsqueda acentuada filtrados en SQL antes de hidratar las entidades. */
    @EntityGraph(attributePaths = {"competicion", "local", "visitante"})
    @Query("""
            select e from Evento e
            where e.estado = :estado and e.fechaHora > :fecha
              and (:deporte is null or e.competicion.deporte = :deporte)
              and (:texto = '' or
                   cast(function('translate', lower(e.local.nombre), 'áéíóúüñ', 'aeiouun') as string)
                       like concat('%', :texto, '%') or
                   cast(function('translate', lower(e.visitante.nombre), 'áéíóúüñ', 'aeiouun') as string)
                       like concat('%', :texto, '%') or
                   cast(function('translate', lower(e.competicion.nombre), 'áéíóúüñ', 'aeiouun') as string)
                       like concat('%', :texto, '%'))
            order by e.fechaHora asc, e.id asc
            """)
    Slice<Evento> paginaCatalogo(@Param("estado") EstadoEvento estado, @Param("fecha") LocalDateTime fecha,
                                @Param("deporte") Deporte deporte, @Param("texto") String texto,
                                Pageable pageable);

    /** Próximos eventos paginados para no hidratar el catálogo entero en la portada. */
    @EntityGraph(attributePaths = {"competicion", "local", "visitante"})
    List<Evento> findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento estado, LocalDateTime fecha,
                                                                  Pageable pageable);

    /** Próximo evento de un deporte, limitado en base de datos para seleccionar el destacado. */
    @EntityGraph(attributePaths = {"competicion", "local", "visitante"})
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

    /**
     * Hora del primer partido de un deporte entre dos fechas, sin contar los de un estado (los suspendidos); null si
     * no hay ninguno. Es cuando empieza la jornada de la liga fantasy.
     */
    @Query("""
            select min(e.fechaHora) from Evento e where e.local.deporte = :deporte and e.estado in :estados
            and e.fechaHora >= :desde and e.fechaHora < :hasta""")
    LocalDateTime primerPartidoEntre(@Param("deporte") Deporte deporte,
                                     @Param("estados") List<EstadoEvento> estados,
                                     @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    /** Partidos ya jugados entre dos equipos, en cualquier campo (HU-33). */
    @Query("""
            select e from Evento e where e.estado = es.ucm.fdi.is1.apuestas.eventos.EstadoEvento.FINALIZADO
            and ((e.local = :uno and e.visitante = :otro) or (e.local = :otro and e.visitante = :uno))
            order by e.fechaHora desc""")
    List<Evento> enfrentamientos(Equipo uno, Equipo otro);

    /** Eventos de una competición en un estado; con FINALIZADO sirve para calcular la clasificación (HU-31). */
    List<Evento> findByCompeticionAndEstado(Competicion competicion, EstadoEvento estado);
}

package es.ucm.fdi.is1.apuestas.eventos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

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
}

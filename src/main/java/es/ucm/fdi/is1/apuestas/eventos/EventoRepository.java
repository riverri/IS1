package es.ucm.fdi.is1.apuestas.eventos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento estado, LocalDateTime fecha);

    List<Evento> findAllByOrderByFechaHoraAsc();
}

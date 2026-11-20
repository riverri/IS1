package es.ucm.fdi.is1.apuestas.apuesta;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** Repositorio de {@link Supercuota}. */
public interface SupercuotaRepository extends JpaRepository<Supercuota, Long> {

    @EntityGraph(attributePaths = {"evento.local", "evento.visitante", "evento.competicion"})
    List<Supercuota> findByEventoAndHastaAfter(Evento evento, LocalDateTime ahora);

    @EntityGraph(attributePaths = {"evento.local", "evento.visitante", "evento.competicion"})
    List<Supercuota> findByHastaAfterOrderByHastaAsc(LocalDateTime ahora);

    /** Supercuotas candidatas de varios eventos, para no consultar una por cada tarjeta del catálogo. */
    @EntityGraph(attributePaths = {"evento.local", "evento.visitante", "evento.competicion"})
    List<Supercuota> findByEventoInAndHastaAfter(List<Evento> eventos, LocalDateTime ahora);

    void deleteByEvento(Evento evento);
}

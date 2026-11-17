package es.ucm.fdi.is1.apuestas.apuesta;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** Repositorio de {@link Supercuota}. */
public interface SupercuotaRepository extends JpaRepository<Supercuota, Long> {

    List<Supercuota> findByEventoAndHastaAfter(Evento evento, LocalDateTime ahora);

    List<Supercuota> findByHastaAfterOrderByHastaAsc(LocalDateTime ahora);

    void deleteByEvento(Evento evento);
}

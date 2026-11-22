package es.ucm.fdi.is1.apuestas.apuesta;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** Repositorio de {@link Supercuota}. */
public interface SupercuotaRepository extends JpaRepository<Supercuota, Long> {

    @EntityGraph(attributePaths = {"evento.local", "evento.visitante", "evento.competicion"})
    List<Supercuota> findByEventoAndHastaAfter(Evento evento, LocalDateTime ahora);

    /** Lote ordenado para portada; permite parar al reunir las seis ofertas visibles. */
    @EntityGraph(attributePaths = {"evento.local", "evento.visitante", "evento.competicion"})
    Slice<Supercuota> findByHastaAfterOrderByHastaAscIdAsc(LocalDateTime ahora, Pageable pageable);

    /** Supercuotas candidatas de varios eventos, para no consultar una por cada tarjeta del catálogo. */
    @EntityGraph(attributePaths = {"evento.local", "evento.visitante", "evento.competicion"})
    List<Supercuota> findByEventoInAndHastaAfter(List<Evento> eventos, LocalDateTime ahora);

    /** Las de un partido y resultado, también las que ya han caducado (para saber si una apuesta llevaba una). */
    List<Supercuota> findByEventoAndResultado(Evento evento, Resultado resultado);

    /** Las de varios partidos, también las caducadas (ofertas de cobro anticipado en lote). */
    List<Supercuota> findByEventoIn(List<Evento> eventos);

    void deleteByEvento(Evento evento);
}

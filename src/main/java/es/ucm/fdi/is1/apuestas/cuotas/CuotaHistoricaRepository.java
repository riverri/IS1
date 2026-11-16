package es.ucm.fdi.is1.apuestas.cuotas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** Repositorio de {@link CuotaHistorica}. */
public interface CuotaHistoricaRepository extends JpaRepository<CuotaHistorica, Long> {

    Optional<CuotaHistorica> findTopByEventoOrderByFechaDescIdDesc(Evento evento);

    List<CuotaHistorica> findByEventoOrderByFechaAscIdAsc(Evento evento);

    void deleteByEvento(Evento evento);
}

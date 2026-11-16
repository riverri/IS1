package es.ucm.fdi.is1.apuestas.seguimiento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio de {@link AlertaCuota}. */
public interface AlertaCuotaRepository extends JpaRepository<AlertaCuota, Long> {

    List<AlertaCuota> findByEventoIdAndAvisadaIsNull(Long eventoId);

    List<AlertaCuota> findByAvisadaIsNull();

    List<AlertaCuota> findByUsuarioAndEventoOrderByIdAsc(Usuario usuario, Evento evento);

    void deleteByEvento(Evento evento);
}

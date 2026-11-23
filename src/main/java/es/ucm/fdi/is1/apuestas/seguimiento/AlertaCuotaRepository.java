package es.ucm.fdi.is1.apuestas.seguimiento;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio de {@link AlertaCuota}. */
public interface AlertaCuotaRepository extends JpaRepository<AlertaCuota, Long> {

    List<AlertaCuota> findByEventoIdAndAvisadaIsNull(Long eventoId);

    /**
     * Marca la alerta como avisada solo si aún no lo estaba; devuelve 1 si la ha marcado esta llamada. Con dos apuestas
     * a la vez al mismo partido (en PostgreSQL pueden ir en paralelo) se avisaba dos veces: así solo avisa una.
     */
    @Modifying
    @Query("update AlertaCuota a set a.avisada = :ahora where a.id = :id and a.avisada is null")
    int marcarAvisada(@Param("id") Long id, @Param("ahora") LocalDateTime ahora);

    List<AlertaCuota> findByAvisadaIsNull();

    List<AlertaCuota> findByUsuarioAndEventoOrderByIdAsc(Usuario usuario, Evento evento);

    void deleteByEvento(Evento evento);

    void deleteByUsuario(Usuario usuario);
}

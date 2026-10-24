package es.ucm.fdi.is1.apuestas.cripto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Repositorio Spring Data del histórico de precios (HU-69). */
public interface PrecioCriptoRepository extends JpaRepository<PrecioCripto, Long> {

    /** Los precios desde una fecha, del más antiguo al más reciente: los puntos del gráfico. */
    List<PrecioCripto> findByCriptoAndFechaAfterOrderByFechaAsc(Criptomoneda cripto, LocalDateTime desde);

    /** El último guardado, para no guardar uno en cada descarga. */
    Optional<PrecioCripto> findTopByCriptoOrderByFechaDesc(Criptomoneda cripto);

    /** Borra los anteriores a una fecha: solo se guardan los de la última semana. */
    @Modifying
    @Query("delete from PrecioCripto p where p.fecha < :antes")
    int borrarAnteriores(LocalDateTime antes);
}

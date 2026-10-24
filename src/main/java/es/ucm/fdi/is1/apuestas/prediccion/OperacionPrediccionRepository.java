package es.ucm.fdi.is1.apuestas.prediccion;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Repositorio Spring Data de las compras y ventas en los mercados de predicción (HU-67). */
public interface OperacionPrediccionRepository extends JpaRepository<OperacionPrediccion, Long> {

    /** En orden, para el gráfico de la evolución del precio. */
    List<OperacionPrediccion> findByMercadoOrderByFechaAscIdAsc(MercadoPrediccion mercado);

    /** Usuarios distintos que han operado en el mercado. */
    @Query("select count(distinct o.usuario) from OperacionPrediccion o where o.mercado = :mercado")
    long participantes(MercadoPrediccion mercado);

    /** Volumen: todo el dinero que ha movido el mercado, compras y ventas. */
    @Query("select coalesce(sum(o.importe), 0) from OperacionPrediccion o where o.mercado = :mercado")
    BigDecimal volumen(MercadoPrediccion mercado);

    /** Para el panel de la casa: [tipo, suma de importes] de todas las operaciones. */
    @Query("select o.tipo, coalesce(sum(o.importe), 0) from OperacionPrediccion o group by o.tipo")
    List<Object[]> importesPorTipo();
}

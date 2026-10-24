package es.ucm.fdi.is1.apuestas.cripto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio Spring Data de las compras y ventas de criptomonedas. */
public interface OperacionCriptoRepository extends JpaRepository<OperacionCripto, Long> {

    /** Las últimas 50 operaciones de un usuario, de la más reciente a la más antigua. */
    List<OperacionCripto> findTop50ByUsuarioOrderByFechaDescIdDesc(Usuario usuario);

    /** Para el panel de la casa: por cada tipo, la suma de los importes y la de las comisiones. */
    @Query("select o.tipo, coalesce(sum(o.importe), 0), coalesce(sum(o.comision), 0) from OperacionCripto o"
            + " group by o.tipo")
    List<Object[]> importesPorTipo();
}

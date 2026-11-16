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

    /**
     * Para el ranking por tipo de juego (HU-84): por cada usuario y tipo, el número de operaciones y la suma de los
     * importes de las criptos de esa categoría. Fila = [id del usuario, tipo, operaciones, importe].
     */
    @Query("select o.usuario.id, o.tipo, count(o), coalesce(sum(o.importe), 0) from OperacionCripto o"
            + " where o.cripto.categoria = :categoria group by o.usuario.id, o.tipo")
    List<Object[]> resumenPorUsuario(CategoriaCripto categoria);
}

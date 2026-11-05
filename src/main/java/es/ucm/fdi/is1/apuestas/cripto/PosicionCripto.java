package es.ucm.fdi.is1.apuestas.cripto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entidad JPA: cuánto tiene un usuario de una criptomoneda (HU-72). Una por usuario y cripto; las compras se suman.
 *
 * <p>{@code coste} es lo que le ha costado lo que todavía tiene: al vender una parte, el coste baja en la misma
 * proporción (precio medio de compra). Así la ganancia o pérdida es siempre valor actual − coste.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "cripto_id"}))
public class PosicionCripto {

    /** Decimales de la cantidad: con memecoins de 0,00001 € se compran millones de unidades, o fracciones de bitcoin. */
    static final int DECIMALES_CANTIDAD = 12;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Criptomoneda cripto;

    @Column(nullable = false, precision = 38, scale = 12)
    private BigDecimal cantidad = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal coste = BigDecimal.ZERO;

    protected PosicionCripto() {
        // requerido por JPA
    }

    PosicionCripto(Usuario usuario, Criptomoneda cripto) {
        this.usuario = usuario;
        this.cripto = cripto;
    }

    void sumar(BigDecimal comprada, BigDecimal pagado) {
        cantidad = cantidad.add(comprada);
        coste = coste.add(pagado);
    }

    /** Quita lo vendido; el coste baja en proporción. Que no venda más de lo que tiene lo comprueba el servicio. */
    void restar(BigDecimal vendida) {
        BigDecimal parte = cantidad.signum() == 0 ? BigDecimal.ONE
                : vendida.divide(cantidad, 12, RoundingMode.HALF_UP);
        coste = coste.subtract(coste.multiply(parte)).setScale(2, RoundingMode.HALF_UP);
        cantidad = cantidad.subtract(vendida);
        if (cantidad.signum() == 0) {
            coste = BigDecimal.ZERO;
        }
    }

    /** Valor a precio actual, sin comisión; 0 si aún no hay precio. */
    public BigDecimal getValor() {
        return cripto.getPrecio() == null ? BigDecimal.ZERO
                : cantidad.multiply(cripto.getPrecio()).setScale(2, RoundingMode.DOWN);
    }

    /** Ganancia (positiva) o pérdida (negativa) si se vendiera ahora, sin contar la comisión de venta. */
    public BigDecimal getGanancia() {
        return getValor().subtract(coste);
    }

    /** Ganancia o pérdida en %, con un decimal; null si no hay coste. */
    public BigDecimal getRentabilidad() {
        return coste.signum() == 0 ? null
                : getGanancia().multiply(BigDecimal.valueOf(100)).divide(coste, 1, RoundingMode.HALF_UP);
    }

    public boolean isConCantidad() {
        return cantidad.signum() > 0;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Criptomoneda getCripto() {
        return cripto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getCoste() {
        return coste;
    }
}

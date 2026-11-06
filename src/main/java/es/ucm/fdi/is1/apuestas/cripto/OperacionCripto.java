package es.ucm.fdi.is1.apuestas.cripto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Entidad JPA: una compra o venta de criptomoneda, con el precio al que se hizo (HU-70, HU-71). No se modifica. */
@Entity
public class OperacionCripto {

    /** Compra o venta. */
    public enum Tipo {
        COMPRA, VENTA
    }

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Criptomoneda cripto;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    @Column(nullable = false, precision = 38, scale = 12)
    private BigDecimal cantidad;

    @Column(nullable = false, precision = 30, scale = 12)
    private BigDecimal precio;

    /** Lo cobrado (compra, con comisión) o lo pagado al usuario (venta, sin ella). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal comision;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected OperacionCripto() {
        // requerido por JPA
    }

    OperacionCripto(Usuario usuario, Criptomoneda cripto, Tipo tipo, BigDecimal cantidad, BigDecimal precio,
                    BigDecimal importe, BigDecimal comision, LocalDateTime fecha) {
        this.usuario = usuario;
        this.cripto = cripto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.precio = precio;
        this.importe = importe;
        this.comision = comision;
        this.fecha = fecha;
    }

    public Criptomoneda getCripto() {
        return cripto;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

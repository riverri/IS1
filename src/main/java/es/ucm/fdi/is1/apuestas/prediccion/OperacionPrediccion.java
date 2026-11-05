package es.ucm.fdi.is1.apuestas.prediccion;

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

/**
 * Entidad JPA: una compra o una venta en un mercado de predicción. Es el historial con el que se calculan las
 * estadísticas y la evolución del precio (HU-67). No se modifica nunca.
 */
@Entity
public class OperacionPrediccion {

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
    private MercadoPrediccion mercado;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Lado lado;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal participaciones;

    /** Lo cobrado (compra, con la comisión) o lo pagado al usuario (venta, ya sin ella). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal comision;

    /** Precio del sí justo después de la operación: los puntos del gráfico (CA-67.1). */
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal precioSiDespues;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected OperacionPrediccion() {
        // requerido por JPA
    }

    OperacionPrediccion(Usuario usuario, MercadoPrediccion mercado, Lado lado, Tipo tipo, BigDecimal participaciones,
                        BigDecimal importe, BigDecimal comision, LocalDateTime fecha) {
        this.usuario = usuario;
        this.mercado = mercado;
        this.lado = lado;
        this.tipo = tipo;
        this.participaciones = participaciones;
        this.importe = importe;
        this.comision = comision;
        this.precioSiDespues = mercado.getPrecioSi();
        this.fecha = fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Lado getLado() {
        return lado;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public BigDecimal getParticipaciones() {
        return participaciones;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public BigDecimal getPrecioSiDespues() {
        return precioSiDespues;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Entidad JPA: una línea del libro de movimientos del saldo (HU-55). Guarda cuánto cambió el saldo, por qué y
 * cómo quedó después, así el saldo de cada cuenta se puede explicar movimiento a movimiento.
 *
 * <p>Solo la crea {@link Usuario} cada vez que cambia su saldo (por eso el constructor no es público) y no se
 * modifica nunca: es un registro histórico. Se guarda con el usuario en la misma transacción; si la operación
 * falla, el movimiento desaparece con ella.
 */
@Entity
public class MovimientoSaldo {

    /** Largo máximo del concepto; uno más largo se corta y termina en "…". */
    static final int LARGO_CONCEPTO = 200;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    /** Cuánto cambió el saldo: positivo si entra dinero y negativo si sale. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;

    /** Saldo del usuario justo después de este movimiento. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoResultante;

    /** Detalle para el usuario, por ejemplo "Apuesta a Real Madrid (Real Madrid – Getafe CF)". */
    @Column(nullable = false, length = LARGO_CONCEPTO)
    private String concepto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected MovimientoSaldo() {
        // requerido por JPA
    }

    MovimientoSaldo(Usuario usuario, TipoMovimiento tipo, BigDecimal importe, BigDecimal saldoResultante,
                    String concepto, LocalDateTime fecha) {
        this.usuario = usuario;
        this.tipo = tipo;
        this.importe = importe;
        this.saldoResultante = saldoResultante;
        // Mismo criterio que Notificacion: un texto demasiado largo se corta en lugar de fallar
        this.concepto = concepto.length() > LARGO_CONCEPTO
                ? concepto.substring(0, LARGO_CONCEPTO - 1) + "…" : concepto;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public BigDecimal getSaldoResultante() {
        return saldoResultante;
    }

    public String getConcepto() {
        return concepto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

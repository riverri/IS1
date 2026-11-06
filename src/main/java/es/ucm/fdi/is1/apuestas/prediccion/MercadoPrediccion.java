package es.ucm.fdi.is1.apuestas.prediccion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/**
 * Entidad JPA: mercado de predicción de sí o no, tipo Polymarket (HU-63 a HU-67). Por ejemplo, "¿Ganará el Real
 * Madrid LaLiga 2026/27?". No es el mercado a largo plazo de {@code mercados.Mercado}: aquí no hay cuotas fijas, sino
 * un precio entre 0 y 1 que se mueve con cada compra y venta según el {@link Lmsr}.
 *
 * <p>Guarda cuántas participaciones se han vendido de cada lado ({@code qSi}, {@code qNo}); de ahí sale el precio.
 * {@code @Version} hace fallar la segunda de dos operaciones a la vez sobre el mismo mercado, así el precio nunca se
 * calcula con datos viejos.
 */
@Entity
public class MercadoPrediccion {

    /** Decimales de las participaciones: se redondean hacia abajo, nunca a favor del usuario. */
    static final int DECIMALES_PARTICIPACIONES = 6;

    @Version
    private long version;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 200)
    private String pregunta;

    /** Cómo se decide la respuesta; opcional. */
    @Column(length = 500)
    private String descripcion;

    /** Último momento para comprar o vender. */
    @Column(nullable = false)
    private LocalDateTime cierre;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoPrediccion estado = EstadoPrediccion.ABIERTO;

    /** Respuesta correcta; null hasta que el creador lo resuelve. */
    @Enumerated(EnumType.STRING)
    private Lado resultado;

    /** Liquidez b del LMSR: con más liquidez el precio se mueve menos con cada compra. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal liquidez;

    /** Parte de cada compra y venta que se queda la casa (0,02 = 2 %). */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal comision;

    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal qSi = BigDecimal.ZERO;

    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal qNo = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime creado;

    protected MercadoPrediccion() {
        // requerido por JPA
    }

    /**
     * Mercado nuevo, abierto y con el precio en 0,50 (CA-63.1).
     *
     * @throws IllegalArgumentException si la liquidez no es positiva o la comisión no está entre 0 y 10 %
     */
    public MercadoPrediccion(String pregunta, String descripcion, LocalDateTime cierre, BigDecimal liquidez,
                             BigDecimal comision, LocalDateTime creado) {
        if (liquidez == null || liquidez.compareTo(BigDecimal.TEN) < 0) {
            throw new IllegalArgumentException("La liquidez tiene que ser al menos 10");
        }
        if (comision == null || comision.signum() < 0 || comision.compareTo(new BigDecimal("0.10")) > 0) {
            throw new IllegalArgumentException("La comisión va del 0 al 10 %");
        }
        this.pregunta = pregunta.trim();
        this.descripcion = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        this.cierre = cierre;
        this.liquidez = liquidez;
        this.comision = comision;
        this.creado = creado;
    }

    /** Se compra y se vende mientras esté abierto y no haya llegado la fecha de cierre (CA-63.2). */
    public boolean admiteOperaciones(LocalDateTime ahora) {
        return estado == EstadoPrediccion.ABIERTO && cierre.isAfter(ahora);
    }

    /** Precio (probabilidad) del lado, entre 0 y 1. */
    public double precio(Lado lado) {
        double si = Lmsr.precioSi(qSi.doubleValue(), qNo.doubleValue(), liquidez.doubleValue());
        return lado == Lado.SI ? si : 1 - si;
    }

    /** Precio del sí con 4 decimales, para enseñarlo y para comparar con el que vio el usuario (CA-64.3). */
    public BigDecimal getPrecioSi() {
        return BigDecimal.valueOf(precio(Lado.SI)).setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal getPrecioNo() {
        return BigDecimal.ONE.subtract(getPrecioSi());
    }

    /** Participaciones que se recibirían por {@code dinero} moneditas ya sin comisión, sin comprar todavía. */
    BigDecimal cotizarCompra(Lado lado, BigDecimal dinero) {
        double delta = Lmsr.participacionesPorDinero(lado == Lado.SI, dinero.doubleValue(), qSi.doubleValue(),
                qNo.doubleValue(), liquidez.doubleValue());
        return BigDecimal.valueOf(delta).setScale(DECIMALES_PARTICIPACIONES, RoundingMode.DOWN);
    }

    /** Dinero que devolvería vender esas participaciones, antes de la comisión, sin vender todavía. */
    BigDecimal cotizarVenta(Lado lado, BigDecimal participaciones) {
        double dinero = Lmsr.dineroPorVenta(lado == Lado.SI, participaciones.doubleValue(), qSi.doubleValue(),
                qNo.doubleValue(), liquidez.doubleValue());
        return BigDecimal.valueOf(dinero).setScale(2, RoundingMode.DOWN);
    }

    /** Compra: las participaciones se suman a las vendidas de ese lado y el precio sube (CA-64.1). */
    BigDecimal comprar(Lado lado, BigDecimal dinero) {
        BigDecimal participaciones = cotizarCompra(lado, dinero);
        if (lado == Lado.SI) {
            qSi = qSi.add(participaciones);
        } else {
            qNo = qNo.add(participaciones);
        }
        return participaciones;
    }

    /** Venta: las participaciones vuelven al creador de mercado y el precio baja (CA-65.1). */
    BigDecimal vender(Lado lado, BigDecimal participaciones) {
        BigDecimal dinero = cotizarVenta(lado, participaciones);
        if (lado == Lado.SI) {
            qSi = qSi.subtract(participaciones);
        } else {
            qNo = qNo.subtract(participaciones);
        }
        return dinero;
    }

    /** @throws IllegalStateException si no está abierto */
    void cerrar() {
        if (estado != EstadoPrediccion.ABIERTO) {
            throw new IllegalStateException("Solo se puede cerrar un mercado abierto");
        }
        estado = EstadoPrediccion.CERRADO;
    }

    /**
     * Marca (o corrige) la respuesta (HU-66). No se puede en un mercado anulado.
     *
     * @return true si ya estaba resuelto y esto es una corrección
     */
    boolean resolver(Lado respuesta) {
        if (estado == EstadoPrediccion.ANULADO) {
            throw new IllegalStateException("Un mercado anulado no puede tener respuesta");
        }
        boolean correccion = estado == EstadoPrediccion.RESUELTO;
        resultado = respuesta;
        estado = EstadoPrediccion.RESUELTO;
        return correccion;
    }

    /** @throws IllegalStateException si ya está resuelto o anulado (uno resuelto se corrige, no se anula) */
    void anular() {
        if (estado == EstadoPrediccion.RESUELTO || estado == EstadoPrediccion.ANULADO) {
            throw new IllegalStateException("No se puede anular un mercado resuelto o ya anulado");
        }
        estado = EstadoPrediccion.ANULADO;
    }

    /** Lo más que puede perder la casa en este mercado (b · ln 2), para Gestión. */
    public BigDecimal getPerdidaMaxima() {
        return BigDecimal.valueOf(Lmsr.perdidaMaxima(liquidez.doubleValue())).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public String getPregunta() {
        return pregunta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getCierre() {
        return cierre;
    }

    public EstadoPrediccion getEstado() {
        return estado;
    }

    public Lado getResultado() {
        return resultado;
    }

    public BigDecimal getLiquidez() {
        return liquidez;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public BigDecimal getQSi() {
        return qSi;
    }

    public BigDecimal getQNo() {
        return qNo;
    }

    public LocalDateTime getCreado() {
        return creado;
    }
}

package es.ucm.fdi.is1.apuestas.prediccion;

import java.math.BigDecimal;
import java.math.RoundingMode;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entidad JPA: cuántas participaciones de un lado de un mercado tiene un usuario (HU-64, HU-65). Hay una por usuario,
 * mercado y lado; si compra más, se suman.
 *
 * <p>{@code invertido} es lo que ha puesto de su saldo menos lo que ha recuperado vendiendo: es lo que se le devuelve
 * si el mercado se anula (CA-63.3). {@code pagado} es lo cobrado al resolverse, para corregir por la diferencia como
 * {@code Apuesta.pagado} (HU-66, CA-66.2).
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "mercado_id", "lado"}))
public class PosicionPrediccion {

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

    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal participaciones = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal invertido = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal pagado = BigDecimal.ZERO;

    protected PosicionPrediccion() {
        // requerido por JPA
    }

    PosicionPrediccion(Usuario usuario, MercadoPrediccion mercado, Lado lado) {
        this.usuario = usuario;
        this.mercado = mercado;
        this.lado = lado;
    }

    void sumar(BigDecimal nuevas, BigDecimal pagadoPorEllas) {
        participaciones = participaciones.add(nuevas);
        invertido = invertido.add(pagadoPorEllas);
    }

    /** Lo comprueba antes {@code PrediccionService.vender}: nunca se venden más de las que hay (CA-65.2). */
    void restar(BigDecimal vendidas, BigDecimal recuperado) {
        participaciones = participaciones.subtract(vendidas);
        invertido = invertido.subtract(recuperado);
    }

    /** Lo que le toca cobrar al resolverse con esa respuesta: 1 monedita por participación ganadora. */
    BigDecimal debeCobrar(Lado respuesta) {
        return lado == respuesta ? participaciones.setScale(2, RoundingMode.DOWN) : BigDecimal.ZERO;
    }

    void marcarPagado(BigDecimal cantidad) {
        pagado = cantidad;
    }

    /** Valor si se vendiera ahora a precio de mercado (sin comisión), para la cartera y las ligas (CA-68.1). */
    public BigDecimal getValorActual() {
        return participaciones.multiply(BigDecimal.valueOf(mercado.precio(lado))).setScale(2, RoundingMode.DOWN);
    }

    public boolean isConParticipaciones() {
        return participaciones.signum() > 0;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public MercadoPrediccion getMercado() {
        return mercado;
    }

    public Lado getLado() {
        return lado;
    }

    public BigDecimal getParticipaciones() {
        return participaciones;
    }

    public BigDecimal getInvertido() {
        return invertido;
    }

    public BigDecimal getPagado() {
        return pagado;
    }
}

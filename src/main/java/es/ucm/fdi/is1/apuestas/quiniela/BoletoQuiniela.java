package es.ucm.fdi.is1.apuestas.quiniela;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/**
 * Entidad JPA: boleto de un usuario en una {@link Quiniela} (HU-79). Uno por usuario y quiniela (restricción única de
 * la migración V16). Los pronósticos se guardan como texto, un carácter por partido en el orden del boleto: "1X2".
 */
@Entity
public class BoletoQuiniela {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Quiniela quiniela;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Column(nullable = false, length = Quiniela.MAX_PARTIDOS)
    private String pronosticos;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pagado;

    /** Null hasta que se reparte. */
    private Integer aciertos;

    /** Lo que recibe al repartir: su parte del bote o, si se devuelve, el precio. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal premio = BigDecimal.ZERO;

    /** Se devolvió el precio: la quiniela se anuló o nadie acertó ninguno (CA-80.3). */
    @Column(nullable = false)
    private boolean devuelto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected BoletoQuiniela() {
        // requerido por JPA
    }

    BoletoQuiniela(Quiniela quiniela, Usuario usuario, List<Resultado> pronosticos, LocalDateTime fecha) {
        this.quiniela = quiniela;
        this.usuario = usuario;
        this.pronosticos = String.join("", pronosticos.stream().map(Resultado::getSimbolo).toList());
        this.pagado = quiniela.getPrecio();
        this.fecha = fecha;
    }

    /** Pronóstico del partido en la posición {@code orden}. */
    public Resultado pronostico(int orden) {
        String simbolo = String.valueOf(pronosticos.charAt(orden));
        for (Resultado r : Resultado.values()) {
            if (r.getSimbolo().equals(simbolo)) {
                return r;
            }
        }
        throw new IllegalStateException("Pronóstico desconocido: " + simbolo);
    }

    /** Cuenta los aciertos con los partidos ya jugados (CA-80.2: un anulado es acierto). */
    int contarAciertos() {
        aciertos = (int) quiniela.getPartidos().stream().filter(p -> p.acierta(pronostico(p.getOrden()))).count();
        return aciertos;
    }

    /** Si acierta algún partido que se ha jugado (sin contar los anulados). */
    boolean aciertaAlgunJugado() {
        return quiniela.getPartidos().stream().anyMatch(p -> p.getEvento().getEstado() == EstadoEvento.FINALIZADO
                && p.acierta(pronostico(p.getOrden())));
    }

    void premiar(BigDecimal parte) {
        premio = parte;
    }

    void devolver() {
        premio = pagado;
        devuelto = true;
    }

    /** Ha cobrado parte del bote (no cuenta la devolución). */
    public boolean isGanador() {
        return !devuelto && premio.signum() > 0;
    }

    /** Lo ganado menos lo pagado; 0 si se devolvió. */
    public BigDecimal getBeneficio() {
        return devuelto ? BigDecimal.ZERO : premio.subtract(pagado);
    }

    public Long getId() {
        return id;
    }

    public Quiniela getQuiniela() {
        return quiniela;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getPronosticos() {
        return pronosticos;
    }

    public BigDecimal getPagado() {
        return pagado;
    }

    public Integer getAciertos() {
        return aciertos;
    }

    public BigDecimal getPremio() {
        return premio;
    }

    public boolean isDevuelto() {
        return devuelto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

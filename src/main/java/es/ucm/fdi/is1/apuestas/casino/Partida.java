package es.ucm.fdi.is1.apuestas.casino;

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
import jakarta.persistence.Version;

/**
 * Entidad JPA: una partida del casino, de cualquier juego (HU-56, HU-57, HU-58). Es la parte común del "módulo de
 * juego": quién juega, cuánto apuesta, cuánto cobra y cómo quedó. Las reglas de cada juego están aparte
 * ({@link ApuestaRuleta}, {@link ManoBlackjack}); el cobro y el pago los hace {@link CasinoService}.
 *
 * <p>Una mano de blackjack dura varias peticiones (pedir, plantarse…): su estado se guarda aquí, en
 * {@code estadoJuego}, y no en la sesión ni en el navegador. {@code @Version} hace fallar la segunda de dos
 * peticiones a la vez sobre la misma mano (doble clic).
 */
@Entity
public class Partida {

    @Version
    private long version;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoJuego juego;

    /** Lo apostado en total, también lo añadido al doblar. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal apostado;

    /** Lo cobrado (con la apuesta incluida); 0 si se pierde o mientras sigue. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal premio = BigDecimal.ZERO;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoPartida estado = EstadoPartida.EN_CURSO;

    /** A qué se apostó: "Rojo", "Pleno al 17", "Blackjack"… */
    @Column(nullable = false, length = 100)
    private String apuesta;

    /** Cómo acabó: "Salió el 17 (negro)", "Tú 20, banca 19"… Null mientras sigue. */
    @Column(length = 200)
    private String resultado;

    /** Estado interno del juego (la mano de blackjack con su baraja). Nunca se envía a la vista. */
    @Column(length = 2000)
    private String estadoJuego;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Partida() {
        // requerido por JPA
    }

    Partida(Usuario usuario, TipoJuego juego, BigDecimal apostado, String apuesta, LocalDateTime fecha) {
        this.usuario = usuario;
        this.juego = juego;
        this.apostado = apostado;
        this.apuesta = apuesta;
        this.fecha = fecha;
    }

    /** Termina la partida con lo cobrado y cómo acabó. */
    void terminar(BigDecimal cobrado, String comoAcabo) {
        premio = cobrado;
        resultado = comoAcabo;
        estado = EstadoPartida.TERMINADA;
    }

    /** Al doblar en el blackjack se apuesta otra vez lo mismo. */
    void doblarApuesta() {
        apostado = apostado.add(apostado);
    }

    ManoBlackjack mano() {
        return ManoBlackjack.leer(estadoJuego);
    }

    void guardarMano(ManoBlackjack mano) {
        estadoJuego = mano.guardar();
    }

    public boolean isEnCurso() {
        return estado == EstadoPartida.EN_CURSO;
    }

    /** Lo ganado (positivo) o perdido (negativo) en la partida; 0 si sigue en curso. */
    public BigDecimal getBalance() {
        return isEnCurso() ? BigDecimal.ZERO : premio.subtract(apostado);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoJuego getJuego() {
        return juego;
    }

    public BigDecimal getApostado() {
        return apostado;
    }

    public BigDecimal getPremio() {
        return premio;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public String getApuesta() {
        return apuesta;
    }

    public String getResultado() {
        return resultado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

package es.ucm.fdi.is1.apuestas.fantasy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Un jugador en un equipo de la liga fantasy, desde que se ficha hasta que se vende (HU-81). Se guarda también
 * después de venderlo: los puntos que hizo mientras estaba en el equipo siguen contando.
 */
@Entity
public class FichajeFantasy {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private EquipoFantasy equipo;

    @ManyToOne(optional = false)
    private Jugador jugador;

    /** Lo que costó, en millones ficticios: es lo que se recupera al venderlo. */
    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal precio;

    @Column(nullable = false)
    private LocalDateTime desde;

    /** Cuándo se vendió; null mientras siga en el equipo. */
    private LocalDateTime hasta;

    protected FichajeFantasy() {
        // requerido por JPA
    }

    FichajeFantasy(EquipoFantasy equipo, Jugador jugador, BigDecimal precio, LocalDateTime desde) {
        this.equipo = equipo;
        this.jugador = jugador;
        this.precio = precio;
        this.desde = desde;
    }

    void vender(LocalDateTime ahora) {
        hasta = ahora;
    }

    /**
     * Si un partido de su equipo le cuenta: solo los que empiezan con el jugador ya fichado (y antes de venderlo).
     * Así no se puede fichar a alguien cuando ya se sabe cómo ha quedado su partido.
     */
    public boolean cuenta(Evento partido) {
        return !partido.getFechaHora().isBefore(desde) && (hasta == null || partido.getFechaHora().isBefore(hasta));
    }

    public boolean isActivo() {
        return hasta == null;
    }

    public Long getId() {
        return id;
    }

    public EquipoFantasy getEquipo() {
        return equipo;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public LocalDateTime getDesde() {
        return desde;
    }

    public LocalDateTime getHasta() {
        return hasta;
    }
}

package es.ucm.fdi.is1.apuestas.fantasy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    /** El jugador; null si después se borró (de Gestión o porque desapareció de la plantilla de la API). */
    @ManyToOne
    private Jugador jugador;

    /**
     * Club, posición y nombre que tenía al ficharlo: los puntos y la formación salen de aquí y no del jugador, para que
     * un traspaso, un cambio de posición o que se borre al jugador no cambien lo que ya sumó (CA-82.1).
     */
    @ManyToOne(optional = false)
    private Equipo club;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Posicion posicion;

    @Column(nullable = false)
    private String nombreJugador;

    /** Lo que costó, en millones ficticios: es lo que se recupera al venderlo. */
    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal precio;

    /**
     * Desde cuándo suma: al ficharlo o, si la jornada ya había empezado, el lunes siguiente (ver
     * {@link FantasyService.Jornada}).
     */
    @Column(nullable = false)
    private LocalDateTime desde;

    /**
     * Hasta cuándo suma; null mientras siga en el equipo. Al venderlo con la jornada ya empezada es el lunes siguiente:
     * sigue sumando lo que queda de jornada.
     */
    private LocalDateTime hasta;

    protected FichajeFantasy() {
        // requerido por JPA
    }

    FichajeFantasy(EquipoFantasy equipo, Jugador jugador, BigDecimal precio, LocalDateTime desde) {
        this.equipo = equipo;
        this.jugador = jugador;
        this.club = jugador.getEquipo();
        this.posicion = jugador.getPosicion();
        this.nombreJugador = jugador.getNombre();
        this.precio = precio;
        this.desde = desde;
    }

    void vender(LocalDateTime hasta) {
        this.hasta = hasta;
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

    /** Si es ese jugador (false si el jugador ya no existe). */
    public boolean esDe(Long jugadorId) {
        return jugador != null && jugador.getId().equals(jugadorId);
    }

    public Equipo getClub() {
        return club;
    }

    public Posicion getPosicion() {
        return posicion;
    }

    public String getNombreJugador() {
        return nombreJugador;
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

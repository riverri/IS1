package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Apuesta simple: un pronóstico sobre un evento, con la cuota fijada en el momento de apostar (HU-23). */
@Entity
public class Apuesta {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Evento evento;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Resultado pronostico;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal cuota;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoApuesta estado = EstadoApuesta.ACTIVA;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Apuesta() {
        // requerido por JPA
    }

    public Apuesta(Usuario usuario, Evento evento, Resultado pronostico, BigDecimal importe, BigDecimal cuota,
                   LocalDateTime fecha) {
        this.usuario = usuario;
        this.evento = evento;
        this.pronostico = pronostico;
        this.importe = importe;
        this.cuota = cuota;
        this.fecha = fecha;
    }

    /** Lo que cobra el usuario si acierta: importe × cuota (HU-29). */
    public BigDecimal getGananciaPotencial() {
        return importe.multiply(cuota).setScale(2, RoundingMode.DOWN);
    }

    /** Una apuesta activa se puede cancelar mientras el evento no haya empezado (HU-26). */
    public boolean cancelable(LocalDateTime ahora) {
        return estado == EstadoApuesta.ACTIVA && evento.getFechaHora().isAfter(ahora);
    }

    /** Cancela la apuesta y devuelve el importe al usuario. */
    public void cancelar(LocalDateTime ahora) {
        if (!cancelable(ahora)) {
            throw new IllegalStateException("La apuesta ya no se puede cancelar");
        }
        estado = EstadoApuesta.CANCELADA;
        usuario.abonar(importe);
    }

    /** "Real Madrid", "Empate" o el nombre del visitante, según el pronóstico. */
    public String getDescripcionPronostico() {
        return switch (pronostico) {
            case LOCAL -> evento.getLocal().getNombre();
            case EMPATE -> "Empate";
            case VISITANTE -> evento.getVisitante().getNombre();
        };
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public Resultado getPronostico() {
        return pronostico;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public BigDecimal getCuota() {
        return cuota;
    }

    public EstadoApuesta getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Un pronóstico sobre un evento dentro de una apuesta, con la cuota fijada al apostar. */
@Entity
public class Seleccion {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Apuesta apuesta;

    @ManyToOne(optional = false)
    private Evento evento;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Resultado pronostico;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal cuota;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoSeleccion estado = EstadoSeleccion.PENDIENTE;

    protected Seleccion() {
        // requerido por JPA
    }

    Seleccion(Apuesta apuesta, Evento evento, Resultado pronostico, BigDecimal cuota) {
        this.apuesta = apuesta;
        this.evento = evento;
        this.pronostico = pronostico;
        this.cuota = cuota;
    }

    void resolver(Resultado resultado) {
        estado = pronostico == resultado ? EstadoSeleccion.ACERTADA : EstadoSeleccion.FALLADA;
    }

    void anular() {
        estado = EstadoSeleccion.ANULADA;
    }

    /** Cuota que cuenta para el pago: 1,00 si el evento se anuló. */
    BigDecimal getCuotaEfectiva() {
        return estado == EstadoSeleccion.ANULADA ? BigDecimal.ONE : cuota;
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

    public Apuesta getApuesta() {
        return apuesta;
    }

    public Evento getEvento() {
        return evento;
    }

    public Resultado getPronostico() {
        return pronostico;
    }

    public BigDecimal getCuota() {
        return cuota;
    }

    public EstadoSeleccion getEstado() {
        return estado;
    }
}

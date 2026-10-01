package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.mercados.Candidato;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Un pronóstico dentro de una apuesta, con la cuota fijada al apostar: el resultado de un evento (1/X/2)
 * o el ganador de un mercado a largo plazo (HU-44). Tiene evento y pronóstico, o candidato.
 */
@Entity
public class Seleccion {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Apuesta apuesta;

    @ManyToOne
    private Evento evento;

    @Enumerated(EnumType.STRING)
    private Resultado pronostico;

    @ManyToOne
    private Candidato candidato;

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

    Seleccion(Apuesta apuesta, Candidato candidato, BigDecimal cuota) {
        this.apuesta = apuesta;
        this.candidato = candidato;
        this.cuota = cuota;
    }

    /** Apuesta a largo plazo (HU-44) en lugar de un partido. */
    public boolean isLargoPlazo() {
        return candidato != null;
    }

    boolean esDe(Evento otro) {
        return evento != null && evento.getId().equals(otro.getId());
    }

    boolean esDe(Mercado mercado) {
        return candidato != null && candidato.getMercado().getId().equals(mercado.getId());
    }

    /** Mientras el evento no haya empezado o el mercado siga abierto. */
    boolean admiteCancelacion(LocalDateTime ahora) {
        if (candidato != null) {
            return candidato.getMercado().admiteApuestas(ahora);
        }
        return evento.getEstado() == EstadoEvento.PROGRAMADO && evento.getFechaHora().isAfter(ahora);
    }

    void resolver(Candidato ganador) {
        estado = candidato.getId().equals(ganador.getId()) ? EstadoSeleccion.ACERTADA : EstadoSeleccion.FALLADA;
    }

    void resolver(Resultado resultado) {
        estado = pronostico == resultado ? EstadoSeleccion.ACERTADA : EstadoSeleccion.FALLADA;
    }

    void anular() {
        estado = EstadoSeleccion.ANULADA;
    }

    /** Al modificar el importe de la apuesta se aplica la cuota actual (HU-27). */
    void actualizarCuota(BigDecimal nueva) {
        cuota = nueva;
    }

    /** Cuota que cuenta para el pago: 1,00 si el evento se anuló. */
    BigDecimal getCuotaEfectiva() {
        return estado == EstadoSeleccion.ANULADA ? BigDecimal.ONE : cuota;
    }

    /** "Real Madrid", "Empate" o el nombre del visitante, según el pronóstico; o el candidato elegido. */
    public String getDescripcionPronostico() {
        if (candidato != null) {
            return candidato.getNombre();
        }
        return switch (pronostico) {
            case LOCAL -> evento.getLocal().getNombre();
            case EMPATE -> "Empate";
            case VISITANTE -> evento.getVisitante().getNombre();
        };
    }

    /** "1", "X", "2" o un trofeo en las apuestas a largo plazo. */
    public String getSimbolo() {
        return candidato != null ? "🏆" : pronostico.getSimbolo();
    }

    /** "Real Madrid – Getafe CF" o el nombre del mercado ("Balón de Oro 2027"). */
    public String getTitulo() {
        if (candidato != null) {
            return candidato.getMercado().getNombre();
        }
        return evento.getLocal().getNombre() + " – " + evento.getVisitante().getNombre();
    }

    /** Fecha del partido, o la de cierre del mercado. */
    public LocalDateTime getFecha() {
        return candidato != null ? candidato.getMercado().getCierre() : evento.getFechaHora();
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

    public Candidato getCandidato() {
        return candidato;
    }

    public BigDecimal getCuota() {
        return cuota;
    }

    public EstadoSeleccion getEstado() {
        return estado;
    }
}

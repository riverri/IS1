package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
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
 * Un pronóstico dentro de una apuesta, con la cuota fijada al apostar: el resultado de un evento (1/X/2),
 * otro tipo de apuesta del partido (doble oportunidad, goles o ambos marcan, HU-52) o el ganador de un mercado
 * a largo plazo (HU-44). Tiene evento y pronóstico, evento y especial, o candidato.
 *
 * <p>Entidad JPA. Solo la crea su {@link Apuesta} (los constructores no son públicos), que decide su
 * propio estado a partir del de sus selecciones.
 */
@Entity
public class Seleccion {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Apuesta apuesta;

    /** Partido al que se apuesta; null en las apuestas a largo plazo. */
    @ManyToOne
    private Evento evento;

    /** Resultado 1X2 elegido; null si es de un tipo especial o a largo plazo. */
    @Enumerated(EnumType.STRING)
    private Resultado pronostico;

    /** Doble oportunidad, goles o ambos marcan (HU-52); null en las demás. */
    @Enumerated(EnumType.STRING)
    private Especial especial;

    /** Candidato elegido de un mercado; solo en las apuestas a largo plazo. */
    @ManyToOne
    private Candidato candidato;

    /** Cuota fijada al apostar; solo cambia si se modifica el importe (HU-27). */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal cuota;

    /** Pendiente hasta que se conoce el resultado del evento o el ganador del mercado. */
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

    Seleccion(Apuesta apuesta, Evento evento, Especial especial, BigDecimal cuota) {
        this.apuesta = apuesta;
        this.evento = evento;
        this.especial = especial;
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

    /** Se compara por id porque {@link Evento} no redefine {@code equals}. */
    boolean esDe(Evento otro) {
        return evento != null && evento.getId().equals(otro.getId());
    }

    /** Si es una apuesta a largo plazo a un candidato de ese mercado. */
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

    /** Acertada si el candidato elegido es el ganador del mercado; fallada si no. */
    void resolver(Candidato ganador) {
        estado = candidato.getId().equals(ganador.getId()) ? EstadoSeleccion.ACERTADA : EstadoSeleccion.FALLADA;
    }

    /** Con el resultado del evento y, si se conoce, su marcador (las de goles se anulan sin él). */
    void resolver(Resultado resultado, Integer golesLocal, Integer golesVisitante) {
        // Una 1X2 se acierta si coincide el resultado; una especial lo decide Especial.acierta,
        // que devuelve null si hace falta el marcador y solo se conoce el ganador
        Boolean acierto = especial != null ? especial.acierta(resultado, golesLocal, golesVisitante)
                : Boolean.valueOf(pronostico == resultado);
        if (acierto == null) {
            // No se puede decidir: se anula y cuenta con cuota 1,00 (CA-52.3)
            estado = EstadoSeleccion.ANULADA;
        } else {
            estado = acierto ? EstadoSeleccion.ACERTADA : EstadoSeleccion.FALLADA;
        }
    }

    /** El evento o el mercado se ha anulado: la selección cuenta con cuota 1,00 (HU-05, HU-30). */
    void anular() {
        estado = EstadoSeleccion.ANULADA;
    }

    /** Al modificar el importe de la apuesta se aplica la cuota actual (HU-27). */
    void actualizarCuota(BigDecimal nueva) {
        cuota = nueva;
    }

    /** Cuota que cuenta para el pago: 1,00 si la selección está anulada (evento o mercado anulado, o goles sin marcador). */
    BigDecimal getCuotaEfectiva() {
        return estado == EstadoSeleccion.ANULADA ? BigDecimal.ONE : cuota;
    }

    /** "Real Madrid", "Empate" o el nombre del visitante, según el pronóstico; o el candidato elegido. */
    public String getDescripcionPronostico() {
        if (candidato != null) {
            return candidato.getNombre();
        }
        if (especial != null) {
            return especial.getDescripcion();
        }
        return switch (pronostico) {
            case LOCAL -> evento.getLocal().getNombre();
            case EMPATE -> "Empate";
            case VISITANTE -> evento.getVisitante().getNombre();
        };
    }

    /** "1", "X", "2", el del tipo especial ("1X", "+2,5"…) o un trofeo en las apuestas a largo plazo. */
    public String getSimbolo() {
        if (candidato != null) {
            return "🏆";
        }
        return especial != null ? especial.getSimbolo() : pronostico.getSimbolo();
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

    public Especial getEspecial() {
        return especial;
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

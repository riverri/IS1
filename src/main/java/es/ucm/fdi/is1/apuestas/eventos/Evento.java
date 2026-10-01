package es.ucm.fdi.is1.apuestas.eventos;

import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

/** Partido entre dos equipos de una competición, sobre el que se apuesta (G/E/P). */
@Entity
public class Evento {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    private Competicion competicion;

    @NotNull
    @ManyToOne(optional = false)
    private Equipo local;

    @NotNull
    @ManyToOne(optional = false)
    private Equipo visitante;

    @NotNull
    private LocalDateTime fechaHora;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoEvento estado = EstadoEvento.PROGRAMADO;

    /** Resultado final, una vez que el creador lo introduce (HU-04). */
    @Enumerated(EnumType.STRING)
    private Resultado resultado;

    /** Identificador del partido en la API de datos deportivos. */
    @Column(unique = true)
    private Long idExterno;

    /** Jornada, ronda o gran premio ("Jornada 8", "Cuartos de final"…). Opcional. */
    private String fase;

    protected Evento() {
        // requerido por JPA
    }

    public Evento(Competicion competicion, Equipo local, Equipo visitante, LocalDateTime fechaHora) {
        this.competicion = competicion;
        this.local = local;
        this.visitante = visitante;
        this.fechaHora = fechaHora;
    }

    /** Solo se puede apostar a eventos programados que todavía no han empezado (HU-19). */
    public boolean admiteApuestas(LocalDateTime ahora) {
        return estado == EstadoEvento.PROGRAMADO && fechaHora.isAfter(ahora);
    }

    /** Fija (o corrige) el resultado final y marca el evento como finalizado (HU-04). */
    public void finalizar(Resultado resultadoFinal) {
        if (estado == EstadoEvento.ANULADO) {
            throw new IllegalStateException("Un evento anulado no puede tener resultado");
        }
        if (resultadoFinal == Resultado.EMPATE && !getDeporte().isAdmiteEmpate()) {
            throw new IllegalArgumentException("En " + getDeporte().getNombre() + " no hay empate");
        }
        resultado = resultadoFinal;
        estado = EstadoEvento.FINALIZADO;
    }

    /** Deja de admitir apuestas nuevas hasta que se reactive (HU-05). */
    public void suspender() {
        if (estado != EstadoEvento.PROGRAMADO) {
            throw new IllegalStateException("Solo se puede suspender un evento programado");
        }
        estado = EstadoEvento.SUSPENDIDO;
    }

    public void reactivar() {
        if (estado != EstadoEvento.SUSPENDIDO) {
            throw new IllegalStateException("Solo se puede reactivar un evento suspendido");
        }
        estado = EstadoEvento.PROGRAMADO;
    }

    /** Anula el evento; las apuestas se devuelven desde el servicio (HU-05). */
    public void anular() {
        if (estado == EstadoEvento.FINALIZADO || estado == EstadoEvento.ANULADO) {
            throw new IllegalStateException("No se puede anular un evento finalizado o ya anulado");
        }
        estado = EstadoEvento.ANULADO;
    }

    public boolean haEmpezado(LocalDateTime ahora) {
        return !fechaHora.isAfter(ahora);
    }

    public Resultado getResultado() {
        return resultado;
    }

    public Deporte getDeporte() {
        return competicion.getDeporte();
    }

    public Long getId() {
        return id;
    }

    public Competicion getCompeticion() {
        return competicion;
    }

    public Equipo getLocal() {
        return local;
    }

    public Equipo getVisitante() {
        return visitante;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    /** La fuente de datos ha cambiado la fecha del partido (solo si aún no se ha jugado). */
    public void cambiarFecha(LocalDateTime nuevaFecha) {
        if (estado == EstadoEvento.PROGRAMADO || estado == EstadoEvento.SUSPENDIDO) {
            fechaHora = nuevaFecha;
        }
    }

    public Long getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(Long idExterno) {
        this.idExterno = idExterno;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }
}

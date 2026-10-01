package es.ucm.fdi.is1.apuestas.eventos;

import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
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

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }
}

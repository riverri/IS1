package es.ucm.fdi.is1.apuestas.ligas;

import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Restricción pagada por un miembro de una liga sobre un compañero y un evento. */
@Entity
public class BloqueoLiga {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Liga liga;
    @ManyToOne(optional = false)
    private Usuario creador;
    @ManyToOne(optional = false)
    private Usuario objetivo;
    @ManyToOne(optional = false)
    private Evento evento;
    /** PARTIDO, nombre de Resultado o nombre de Especial. */
    @Column(nullable = false, length = 20)
    private String alcance;
    @Column(nullable = false)
    private LocalDateTime creada;

    protected BloqueoLiga() { }

    BloqueoLiga(Liga liga, Usuario creador, Usuario objetivo, Evento evento, String alcance, LocalDateTime creada) {
        this.liga = liga;
        this.creador = creador;
        this.objetivo = objetivo;
        this.evento = evento;
        this.alcance = alcance;
        this.creada = creada;
    }

    public Long getId() { return id; }
    public Liga getLiga() { return liga; }
    public Usuario getCreador() { return creador; }
    public Usuario getObjetivo() { return objetivo; }
    public Evento getEvento() { return evento; }
    public String getAlcance() { return alcance; }
    public LocalDateTime getCreada() { return creada; }

    boolean activa(LocalDateTime ahora) {
        return evento.getEstado() == EstadoEvento.PROGRAMADO && evento.getFechaHora().isAfter(ahora);
    }
}

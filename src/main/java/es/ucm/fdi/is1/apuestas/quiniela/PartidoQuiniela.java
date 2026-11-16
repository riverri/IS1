package es.ucm.fdi.is1.apuestas.quiniela;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** Entidad JPA: uno de los partidos de una {@link Quiniela}, con su posición en el boleto (HU-78). */
@Entity
public class PartidoQuiniela {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Quiniela quiniela;

    @ManyToOne(optional = false)
    private Evento evento;

    /** Posición en el boleto, desde 0, en orden de fecha. */
    @Column(nullable = false)
    private int orden;

    protected PartidoQuiniela() {
        // requerido por JPA
    }

    PartidoQuiniela(Quiniela quiniela, Evento evento, int orden) {
        this.quiniela = quiniela;
        this.evento = evento;
        this.orden = orden;
    }

    /** Ya se sabe cómo cuenta: finalizado o anulado. */
    public boolean isJugado() {
        return evento.getEstado() == EstadoEvento.FINALIZADO || evento.getEstado() == EstadoEvento.ANULADO;
    }

    /** Un partido anulado cuenta como acierto para todos (CA-80.2). */
    public boolean acierta(Resultado pronostico) {
        return evento.getEstado() == EstadoEvento.ANULADO
                || (evento.getEstado() == EstadoEvento.FINALIZADO && evento.getResultado() == pronostico);
    }

    public Long getId() {
        return id;
    }

    public Quiniela getQuiniela() {
        return quiniela;
    }

    public Evento getEvento() {
        return evento;
    }

    public int getOrden() {
        return orden;
    }
}

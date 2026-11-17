package es.ucm.fdi.is1.apuestas.cuotas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import es.ucm.fdi.is1.apuestas.eventos.Evento;

/**
 * Entidad JPA: las cuotas 1X2 de un evento en un momento (HU-91). Se guarda una cada vez que cambian; con ellas se
 * dibuja la evolución de la cuota. Las apunta {@link RegistroCuotas}.
 */
@Entity
public class CuotaHistorica {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Evento evento;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "cuota_local", nullable = false, precision = 8, scale = 2)
    private BigDecimal local;

    /** Null en los deportes sin empate. */
    @Column(name = "cuota_empate", precision = 8, scale = 2)
    private BigDecimal empate;

    @Column(name = "cuota_visitante", nullable = false, precision = 8, scale = 2)
    private BigDecimal visitante;

    protected CuotaHistorica() {
        // requerido por JPA
    }

    CuotaHistorica(Evento evento, Cuotas cuotas, LocalDateTime fecha) {
        this.evento = evento;
        this.fecha = fecha;
        this.local = cuotas.local();
        this.empate = cuotas.empate();
        this.visitante = cuotas.visitante();
    }

    /** Si son las mismas cuotas, para no guardar dos puntos iguales seguidos. */
    boolean iguales(Cuotas cuotas) {
        return local.compareTo(cuotas.local()) == 0 && visitante.compareTo(cuotas.visitante()) == 0
                && (empate == null ? cuotas.empate() == null
                : cuotas.empate() != null && empate.compareTo(cuotas.empate()) == 0);
    }

    public Cuotas getCuotas() {
        return new Cuotas(local, empate, visitante);
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Evento getEvento() {
        return evento;
    }
}

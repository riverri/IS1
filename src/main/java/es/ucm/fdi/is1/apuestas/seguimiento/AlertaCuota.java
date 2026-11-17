package es.ucm.fdi.is1.apuestas.seguimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/**
 * Entidad JPA: un usuario quiere que le avisen cuando la cuota de un resultado llegue a un valor (HU-90). Avisa una
 * sola vez: después queda con la fecha del aviso.
 */
@Entity
public class AlertaCuota {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Evento evento;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Resultado resultado;

    /** Cuota a la que avisar: cuando la actual llegue a esta o la pase. */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal cuota;

    @Column(nullable = false)
    private LocalDateTime creada;

    /** Null mientras espera. */
    private LocalDateTime avisada;

    protected AlertaCuota() {
        // requerido por JPA
    }

    AlertaCuota(Usuario usuario, Evento evento, Resultado resultado, BigDecimal cuota, LocalDateTime creada) {
        this.usuario = usuario;
        this.evento = evento;
        this.resultado = resultado;
        this.cuota = cuota;
        this.creada = creada;
    }

    void avisar(LocalDateTime ahora) {
        avisada = ahora;
    }

    public boolean isPendiente() {
        return avisada == null;
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

    public Resultado getResultado() {
        return resultado;
    }

    public BigDecimal getCuota() {
        return cuota;
    }

    public LocalDateTime getAvisada() {
        return avisada;
    }
}

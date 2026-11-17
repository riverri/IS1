package es.ucm.fdi.is1.apuestas.ligas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/**
 * Entidad JPA: reto 1 contra 1 entre dos compañeros de liga a un partido (HU-89). Cada uno pone el mismo importe y
 * elige un resultado distinto; el que acierta se lleva los dos importes, sin comisión de la casa. {@code @Version}
 * evita que se acepte y se rechace a la vez.
 */
@Entity
public class Reto {

    @Version
    private long version;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario retador;

    @ManyToOne(optional = false)
    private Usuario retado;

    @ManyToOne(optional = false)
    private Evento evento;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Resultado pronosticoRetador;

    /** Null hasta que acepta. */
    @Enumerated(EnumType.STRING)
    private Resultado pronosticoRetado;

    /** Lo que pone cada uno. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoReto estado = EstadoReto.PENDIENTE;

    /** Null mientras no haya ganador. */
    @ManyToOne
    private Usuario ganador;

    @Column(nullable = false)
    private LocalDateTime creado;

    protected Reto() {
        // requerido por JPA
    }

    Reto(Usuario retador, Usuario retado, Evento evento, Resultado pronostico, BigDecimal importe, LocalDateTime creado) {
        this.retador = retador;
        this.retado = retado;
        this.evento = evento;
        this.pronosticoRetador = pronostico;
        this.importe = importe;
        this.creado = creado;
    }

    void aceptar(Resultado pronostico) {
        pronosticoRetado = pronostico;
        estado = EstadoReto.ACEPTADO;
    }

    void cerrar(EstadoReto nuevo, Usuario quienGana) {
        estado = nuevo;
        ganador = quienGana;
    }

    /** Lo que se lleva el ganador: los dos importes. */
    public BigDecimal getBote() {
        return importe.add(importe);
    }

    public boolean esDe(Usuario usuario) {
        return retador.getId().equals(usuario.getId()) || retado.getId().equals(usuario.getId());
    }

    public Usuario rival(Usuario usuario) {
        return retador.getId().equals(usuario.getId()) ? retado : retador;
    }

    public Long getId() {
        return id;
    }

    public Usuario getRetador() {
        return retador;
    }

    public Usuario getRetado() {
        return retado;
    }

    public Evento getEvento() {
        return evento;
    }

    public Resultado getPronosticoRetador() {
        return pronosticoRetador;
    }

    public Resultado getPronosticoRetado() {
        return pronosticoRetado;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public EstadoReto getEstado() {
        return estado;
    }

    public Usuario getGanador() {
        return ganador;
    }

    public LocalDateTime getCreado() {
        return creado;
    }
}

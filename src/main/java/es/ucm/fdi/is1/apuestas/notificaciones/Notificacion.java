package es.ucm.fdi.is1.apuestas.notificaciones;

import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Aviso a un usuario, por ejemplo cuando se resuelve una de sus apuestas (HU-37). */
@Entity
public class Notificacion {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Column(nullable = false, length = 500)
    private String texto;

    /** Tipo de aviso, para el icono: "ganada", "perdida", "anulada"… */
    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private boolean leida;

    protected Notificacion() {
        // requerido por JPA
    }

    public Notificacion(Usuario usuario, String tipo, String texto, LocalDateTime fecha) {
        this.usuario = usuario;
        this.tipo = tipo;
        this.texto = texto.length() > 500 ? texto.substring(0, 497) + "…" : texto;
        this.fecha = fecha;
    }

    public void marcarLeida() {
        leida = true;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTexto() {
        return texto;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public boolean isLeida() {
        return leida;
    }
}

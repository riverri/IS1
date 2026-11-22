package es.ucm.fdi.is1.apuestas.usuarios;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Single-use confirmation of a new login email address. */
@Entity
public class CambioCorreoPendiente {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 64)
    private String hash;

    @Column(name = "nuevo_email", nullable = false, length = 254)
    private String nuevoEmail;

    @Column(nullable = false)
    private LocalDateTime caduca;

    @Column(nullable = false)
    private boolean usado;

    protected CambioCorreoPendiente() {
        // Required by JPA.
    }

    public CambioCorreoPendiente(Usuario usuario, String hash, String nuevoEmail, LocalDateTime caduca) {
        this.usuario = usuario;
        this.hash = hash;
        this.nuevoEmail = nuevoEmail;
        this.caduca = caduca;
    }

    public boolean valido(LocalDateTime ahora) {
        return !usado && ahora.isBefore(caduca) && !usuario.isEliminado();
    }

    public void usar() {
        usado = true;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNuevoEmail() {
        return nuevoEmail;
    }
}

package es.ucm.fdi.is1.apuestas.usuarios;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Single-use, time-limited proof that a user can receive mail at their login address. */
@Entity
public class TokenVerificacionCorreo {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 64)
    private String hash;

    @Column(nullable = false)
    private LocalDateTime caduca;

    @Column(nullable = false)
    private boolean usado;

    protected TokenVerificacionCorreo() {
        // Required by JPA.
    }

    public TokenVerificacionCorreo(Usuario usuario, String hash, LocalDateTime caduca) {
        this.usuario = usuario;
        this.hash = hash;
        this.caduca = caduca;
    }

    public boolean valido(LocalDateTime ahora) {
        return !usado && ahora.isBefore(caduca) && !usuario.isEliminado() && !usuario.isEmailVerificado();
    }

    public void usar() {
        usado = true;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}

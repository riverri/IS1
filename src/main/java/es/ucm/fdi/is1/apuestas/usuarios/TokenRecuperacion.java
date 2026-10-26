package es.ucm.fdi.is1.apuestas.usuarios;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Enlace para crear una contraseña nueva (HU-17). Solo se guarda el resumen SHA-256 del código del enlace:
 * quien lea la base de datos no puede usarlo. Caduca y sirve una sola vez.
 * Entidad JPA; la crea y la consulta {@link RecuperacionService}.
 */
@Entity
public class TokenRecuperacion {

    @Id
    @GeneratedValue
    private Long id;

    // Relación "muchos a uno": un usuario puede tener varios tokens y cada token es de un único usuario.
    // En la tabla se guarda como una columna usuario_id que apunta al usuario; optional = false → obligatorio
    @ManyToOne(optional = false)
    private Usuario usuario;

    /** Resumen SHA-256 del código, en hexadecimal (64 caracteres). */
    @Column(nullable = false, unique = true, length = 64)
    private String hash;

    /** Momento hasta el que sirve ({@link RecuperacionService#VALIDEZ} desde que se pidió). */
    @Column(nullable = false)
    private LocalDateTime caduca;

    @Column(nullable = false)
    private boolean usado;

    protected TokenRecuperacion() {
        // requerido por JPA
    }

    public TokenRecuperacion(Usuario usuario, String hash, LocalDateTime caduca) {
        this.usuario = usuario;
        this.hash = hash;
        this.caduca = caduca;
    }

    /** Sirve si no se ha usado, no ha caducado y la cuenta no se ha eliminado (CA-17.2). */
    public boolean valido(LocalDateTime ahora) {
        return !usado && ahora.isBefore(caduca) && !usuario.isEliminado();
    }

    /** Gasta el enlace: ya no vale para otra vez. */
    public void usar() {
        usado = true;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}

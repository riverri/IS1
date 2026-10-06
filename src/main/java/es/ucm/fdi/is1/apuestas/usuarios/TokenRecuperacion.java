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
 */
@Entity
public class TokenRecuperacion {

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

    protected TokenRecuperacion() {
        // requerido por JPA
    }

    public TokenRecuperacion(Usuario usuario, String hash, LocalDateTime caduca) {
        this.usuario = usuario;
        this.hash = hash;
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
}

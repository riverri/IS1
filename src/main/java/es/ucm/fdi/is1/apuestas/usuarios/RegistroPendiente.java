package es.ucm.fdi.is1.apuestas.usuarios;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/** Short-lived email-only registration request. It never stores credentials or profile data. */
@Entity
public class RegistroPendiente {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime caduca;

    protected RegistroPendiente() {
    }

    public RegistroPendiente(String email, String tokenHash, LocalDateTime caduca) {
        this.email = email;
        this.tokenHash = tokenHash;
        this.caduca = caduca;
    }

    public boolean valido(LocalDateTime ahora) {
        return ahora.isBefore(caduca);
    }

    public void renovar(String nuevoTokenHash, LocalDateTime nuevaCaducidad) {
        tokenHash = nuevoTokenHash;
        caduca = nuevaCaducidad;
    }

    public String getEmail() { return email; }
}

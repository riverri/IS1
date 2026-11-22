package es.ucm.fdi.is1.apuestas.usuarios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Validated form for reauthenticated login email changes. */
public class CambioCorreoForm {

    @NotBlank
    @Size(max = 254)
    @Email
    private String nuevoEmail;

    @NotBlank
    private String password;

    public String getNuevoEmail() {
        return nuevoEmail;
    }

    public void setNuevoEmail(String nuevoEmail) {
        this.nuevoEmail = nuevoEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

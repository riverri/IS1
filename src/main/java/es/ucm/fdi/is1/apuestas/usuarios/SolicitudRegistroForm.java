package es.ucm.fdi.is1.apuestas.usuarios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Email-only form used to request a registration confirmation link. */
public class SolicitudRegistroForm {

    @NotBlank(message = "Introduce tu email")
    @Email(message = "El email no es válido")
    @Size(max = 255, message = "Máximo 255 caracteres")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

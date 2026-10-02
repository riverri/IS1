package es.ucm.fdi.is1.apuestas.usuarios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cambio de contraseña (HU-47). Mismas reglas que en el registro. */
public class PasswordForm {

    @NotBlank(message = "Introduce tu contraseña actual")
    private String actual;

    @NotBlank(message = "Introduce la contraseña nueva")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String nueva;

    private String confirmacion;

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getNueva() {
        return nueva;
    }

    public void setNueva(String nueva) {
        this.nueva = nueva;
    }

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String confirmacion) {
        this.confirmacion = confirmacion;
    }
}

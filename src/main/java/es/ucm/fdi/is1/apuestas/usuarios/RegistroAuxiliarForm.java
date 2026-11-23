package es.ucm.fdi.is1.apuestas.usuarios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Formulario auxiliar para crear una cuenta sin confirmación por correo. */
public class RegistroAuxiliarForm {

    @NotBlank(message = "Introduce tu email")
    @Email(message = "El email no es válido")
    @Size(max = 255, message = "Máximo 255 caracteres")
    private String email;

    @NotBlank(message = "Introduce tu nombre")
    @Size(max = 50, message = "Máximo 50 caracteres")
    @NombrePermitido
    private String nombre;

    @NotBlank(message = "Introduce una contraseña")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;

    private String confirmacion;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String confirmacion) {
        this.confirmacion = confirmacion;
    }
}

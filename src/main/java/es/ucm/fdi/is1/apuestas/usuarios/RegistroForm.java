package es.ucm.fdi.is1.apuestas.usuarios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulario de registro (HU-11). Las anotaciones las comprueba {@code @Valid} en
 * {@link UsuariosController}; que la confirmación coincida y que la contraseña quepa en BCrypt
 * se comprueba allí a mano.
 */
public class RegistroForm {

    @NotBlank(message = "Introduce tu nombre")
    @Size(max = 50, message = "Máximo 50 caracteres")
    // Sin palabras ofensivas (issue #75, CA-11.3)
    @NombrePermitido
    private String nombre;

    @NotBlank(message = "Introduce tu email")
    @Email(message = "El email no es válido")
    @Size(max = 255, message = "Máximo 255 caracteres")
    private String email;

    @NotBlank(message = "Introduce una contraseña")
    // 72: el máximo de BCrypt (en bytes, ver Contrasenas)
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;

    /** La contraseña repetida, para evitar erratas. */
    private String confirmacion;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

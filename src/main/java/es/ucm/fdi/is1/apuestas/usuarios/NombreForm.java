package es.ucm.fdi.is1.apuestas.usuarios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Formulario de cambio del nombre visible en Mi cuenta (HU-47). Mismas reglas que en el registro. */
public class NombreForm {

    @NotBlank(message = "Introduce tu nombre")
    @Size(max = 50, message = "Máximo 50 caracteres")
    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

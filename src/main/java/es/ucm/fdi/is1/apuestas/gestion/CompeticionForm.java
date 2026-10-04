package es.ucm.fdi.is1.apuestas.gestion;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public class CompeticionForm {

    @NotBlank(message = "Introduce el nombre")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @NotNull(message = "Elige un deporte")
    private Deporte deporte;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public void setDeporte(Deporte deporte) {
        this.deporte = deporte;
    }
}

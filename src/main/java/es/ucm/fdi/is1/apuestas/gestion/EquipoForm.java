package es.ucm.fdi.is1.apuestas.gestion;

import java.util.HashSet;
import java.util.Set;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public class EquipoForm {

    @NotBlank(message = "Introduce el nombre")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @NotNull(message = "Elige un deporte")
    private Deporte deporte;

    @NotNull(message = "Introduce la calificación")
    @DecimalMin(value = "0.0", message = "La calificación debe estar entre 0 y 10")
    @DecimalMax(value = "10.0", message = "La calificación debe estar entre 0 y 10")
    private Double calidad;

    private Set<Long> competicionIds = new HashSet<>();

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

    public Double getCalidad() {
        return calidad;
    }

    public void setCalidad(Double calidad) {
        this.calidad = calidad;
    }

    public Set<Long> getCompeticionIds() {
        return competicionIds;
    }

    public void setCompeticionIds(Set<Long> competicionIds) {
        this.competicionIds = competicionIds;
    }
}

package es.ucm.fdi.is1.apuestas.equipos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Equipo o deportista sobre el que se puede apostar (HU-01).
 * La calificación de calidad (0-10) es la base del cálculo de cuotas.
 */
@Entity
public class Equipo {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String deporte;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Double calidad;

    protected Equipo() {
        // requerido por JPA
    }

    public Equipo(String nombre, String deporte, Double calidad) {
        this.nombre = nombre;
        this.deporte = deporte;
        this.calidad = calidad;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDeporte() {
        return deporte;
    }

    public Double getCalidad() {
        return calidad;
    }
}

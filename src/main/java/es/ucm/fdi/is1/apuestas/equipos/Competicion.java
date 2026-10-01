package es.ucm.fdi.is1.apuestas.equipos;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Competición (LaLiga, Champions, ACB…) en la que participan equipos (HU-01). */
@Entity
public class Competicion {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Deporte deporte;

    protected Competicion() {
        // requerido por JPA
    }

    public Competicion(String nombre, Deporte deporte) {
        this.nombre = nombre;
        this.deporte = deporte;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Deporte getDeporte() {
        return deporte;
    }
}

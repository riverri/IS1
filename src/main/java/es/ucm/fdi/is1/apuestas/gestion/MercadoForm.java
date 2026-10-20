package es.ucm.fdi.is1.apuestas.gestion;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Formulario de alta de un mercado a largo plazo con sus candidatos (HU-45). Las anotaciones solo
 * comprueban que los campos estén rellenos; {@code MercadoService#crear} lee los candidatos y comprueba
 * que haya al menos 2 y que sus cuotas sean válidas.
 */
public class MercadoForm {

    @NotBlank(message = "Escribe el nombre del mercado")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String nombre;

    @NotNull(message = "Elige un deporte")
    private Deporte deporte;

    /** Último momento para apostar; formato del campo datetime-local de HTML. */
    @NotNull(message = "Introduce la fecha de cierre")
    @Future(message = "La fecha de cierre tiene que ser futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime cierre;

    /** Un candidato por línea: "Nombre; cuota". Se escribe en un área de texto y se lee en el servicio. */
    @NotBlank(message = "Añade los candidatos")
    private String candidatos;

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

    public LocalDateTime getCierre() {
        return cierre;
    }

    public void setCierre(LocalDateTime cierre) {
        this.cierre = cierre;
    }

    public String getCandidatos() {
        return candidatos;
    }

    public void setCandidatos(String candidatos) {
        this.candidatos = candidatos;
    }
}

package es.ucm.fdi.is1.apuestas.gestion;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Formulario con los datos editables de un evento que aún no ha empezado (HU-46). La competición no cambia.
 * Las reglas que dependen del evento (que no haya empezado, que no tenga apuestas si cambian los equipos)
 * las comprueba {@link GestionService#editarEvento}.
 */
public class EdicionEventoForm {

    @NotNull(message = "Elige el equipo local")
    private Long localId;

    @NotNull(message = "Elige el equipo visitante")
    private Long visitanteId;

    // La nueva fecha también tiene que ser futura; mismo formato que el campo datetime-local de HTML
    @NotNull(message = "Introduce la fecha y hora")
    @Future(message = "La fecha debe ser futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHora;

    /** Opcional; vacía deja el evento sin fase. */
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String fase;

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public Long getVisitanteId() {
        return visitanteId;
    }

    public void setVisitanteId(Long visitanteId) {
        this.visitanteId = visitanteId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }
}

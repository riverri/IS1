package es.ucm.fdi.is1.apuestas.gestion;

import es.ucm.fdi.is1.apuestas.equipos.Forma;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos editables de un equipo ya existente: calificación (HU-01), forma reciente (HU-02) y escudo. */
public class EdicionEquipoForm {

    @NotNull(message = "Introduce la calificación")
    @DecimalMin(value = "0.0", message = "La calificación debe estar entre 0 y 10")
    @DecimalMax(value = "10.0", message = "La calificación debe estar entre 0 y 10")
    private Double calidad;

    @NotNull(message = "Elige la forma reciente")
    private Forma forma = Forma.NORMAL;

    /** Vacío para quitar el escudo y volver a mostrar las iniciales. */
    @Size(max = 500, message = "Máximo 500 caracteres")
    @Pattern(regexp = "^$|^https?://\\S+$", message = "Debe ser una dirección que empiece por http:// o https://")
    private String escudoUrl;

    public Double getCalidad() {
        return calidad;
    }

    public void setCalidad(Double calidad) {
        this.calidad = calidad;
    }

    public Forma getForma() {
        return forma;
    }

    public void setForma(Forma forma) {
        this.forma = forma;
    }

    public String getEscudoUrl() {
        return escudoUrl;
    }

    public void setEscudoUrl(String escudoUrl) {
        this.escudoUrl = escudoUrl;
    }
}

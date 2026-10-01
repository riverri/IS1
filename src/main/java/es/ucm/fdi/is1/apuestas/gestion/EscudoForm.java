package es.ucm.fdi.is1.apuestas.gestion;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EscudoForm {

    /** Vacío para quitar el escudo y volver a mostrar las iniciales. */
    @Size(max = 500, message = "Máximo 500 caracteres")
    @Pattern(regexp = "^$|^https?://\\S+$", message = "Debe ser una dirección que empiece por http:// o https://")
    private String escudoUrl;

    public String getEscudoUrl() {
        return escudoUrl;
    }

    public void setEscudoUrl(String escudoUrl) {
        this.escudoUrl = escudoUrl;
    }
}

package es.ucm.fdi.is1.apuestas.gestion;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Formulario de los límites de apuesta que fija el creador (HU-07). Lo usa {@link GestionController};
 * los valores se guardan en la entidad {@code Limites}, que comprueba además que el máximo no sea menor
 * que el mínimo.
 */
public class LimitesForm {

    // Importes en moneditas con céntimos: positivos y con 2 decimales como mucho
    @NotNull(message = "Introduce el importe mínimo")
    @DecimalMin(value = "0.01", message = "Tiene que ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "Como mucho 2 decimales")
    private BigDecimal importeMinimo;

    @NotNull(message = "Introduce el importe máximo")
    @DecimalMin(value = "0.01", message = "Tiene que ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "Como mucho 2 decimales")
    private BigDecimal importeMaximo;

    /** Máximo de selecciones de una combinada: una combinada necesita al menos 2. */
    @NotNull(message = "Introduce el número máximo de selecciones")
    @Min(value = 2, message = "Al menos 2")
    @Max(value = 30, message = "Como mucho 30")
    private Integer maxSelecciones;

    public BigDecimal getImporteMinimo() {
        return importeMinimo;
    }

    public void setImporteMinimo(BigDecimal importeMinimo) {
        this.importeMinimo = importeMinimo;
    }

    public BigDecimal getImporteMaximo() {
        return importeMaximo;
    }

    public void setImporteMaximo(BigDecimal importeMaximo) {
        this.importeMaximo = importeMaximo;
    }

    public Integer getMaxSelecciones() {
        return maxSelecciones;
    }

    public void setMaxSelecciones(Integer maxSelecciones) {
        this.maxSelecciones = maxSelecciones;
    }
}

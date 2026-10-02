package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

/** Límites que se pone el propio usuario (HU-10). Vacío = sin límite. */
public class LimitesPersonalesForm {

    @DecimalMin(value = "1", message = "Como mínimo 1 moneda")
    @Digits(integer = 10, fraction = 2, message = "Como mucho 2 decimales")
    private BigDecimal diario;

    @DecimalMin(value = "1", message = "Como mínimo 1 moneda")
    @Digits(integer = 10, fraction = 2, message = "Como mucho 2 decimales")
    private BigDecimal semanal;

    public BigDecimal getDiario() {
        return diario;
    }

    public void setDiario(BigDecimal diario) {
        this.diario = diario;
    }

    public BigDecimal getSemanal() {
        return semanal;
    }

    public void setSemanal(BigDecimal semanal) {
        this.semanal = semanal;
    }
}

package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public class ApuestaForm {

    @NotNull(message = "Elige un resultado")
    private Resultado resultado;

    @NotNull(message = "Introduce un importe")
    @DecimalMin(value = "0.01", message = "El importe tiene que ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "Como mucho 2 decimales")
    private BigDecimal importe;

    public Resultado getResultado() {
        return resultado;
    }

    public void setResultado(Resultado resultado) {
        this.resultado = resultado;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
}

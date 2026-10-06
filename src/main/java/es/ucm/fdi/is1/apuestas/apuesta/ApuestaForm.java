package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

/**
 * Pronóstico e importe de una apuesta simple. El pronóstico es un resultado (1X2) o un tipo especial (HU-52);
 * en la página se eligen con un único grupo de opciones ({@code opcion}).
 */
public class ApuestaForm {

    private Resultado resultado;

    private Especial especial;

    /** Cuota que tenía delante el usuario (la rellena la página); si ha cambiado, se le avisa en lugar de apostar. */
    private BigDecimal cuotaVista;

    @NotNull(message = "Introduce un importe")
    @DecimalMin(value = "0.01", message = "El importe tiene que ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "Como mucho 2 decimales")
    private BigDecimal importe;

    @AssertTrue(message = "Elige un pronóstico")
    public boolean isOpcionElegida() {
        return resultado != null || especial != null;
    }

    /** "LOCAL", "EMPATE"… o "MAS_2_5", "AMBOS_SI"… */
    public String getOpcion() {
        if (especial != null) {
            return especial.name();
        }
        return resultado != null ? resultado.name() : null;
    }

    public void setOpcion(String opcion) {
        resultado = null;
        especial = null;
        if (opcion == null || opcion.isBlank()) {
            return;
        }
        for (Resultado r : Resultado.values()) {
            if (r.name().equals(opcion)) {
                resultado = r;
                return;
            }
        }
        for (Especial e : Especial.values()) {
            if (e.name().equals(opcion)) {
                especial = e;
                return;
            }
        }
    }

    public Resultado getResultado() {
        return resultado;
    }

    public void setResultado(Resultado resultado) {
        this.resultado = resultado;
    }

    public Especial getEspecial() {
        return especial;
    }

    public void setEspecial(Especial especial) {
        this.especial = especial;
    }

    public BigDecimal getCuotaVista() {
        return cuotaVista;
    }

    public void setCuotaVista(BigDecimal cuotaVista) {
        this.cuotaVista = cuotaVista;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
}

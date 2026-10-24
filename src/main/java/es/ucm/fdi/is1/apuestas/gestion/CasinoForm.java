package es.ucm.fdi.is1.apuestas.gestion;

import java.math.BigDecimal;

import es.ucm.fdi.is1.apuestas.casino.ConfiguracionCasino;
import es.ucm.fdi.is1.apuestas.casino.PagoBlackjack;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Formulario de la configuración del casino (HU-59). Que el máximo no sea menor que el mínimo lo comprueba
 * {@link ConfiguracionCasino#cambiar}.
 */
public class CasinoForm {

    private boolean activo;

    @NotNull(message = "Introduce el importe mínimo")
    @DecimalMin(value = "0.01", message = "Tiene que ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "Como mucho 2 decimales")
    private BigDecimal importeMinimo;

    @NotNull(message = "Introduce el importe máximo")
    @DecimalMin(value = "0.01", message = "Tiene que ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "Como mucho 2 decimales")
    private BigDecimal importeMaximo;

    @NotNull(message = "Indica el número de barajas")
    @Min(value = 1, message = "Entre 1 y 8 barajas")
    @Max(value = 8, message = "Entre 1 y 8 barajas")
    private Integer barajas;

    private boolean bancaPideCon17Blando;

    @NotNull(message = "Elige cuánto paga el blackjack")
    private PagoBlackjack pagoBlackjack;

    /** Formulario relleno con la configuración actual. */
    static CasinoForm de(ConfiguracionCasino configuracion) {
        CasinoForm form = new CasinoForm();
        form.activo = configuracion.isActivo();
        form.importeMinimo = configuracion.getImporteMinimo();
        form.importeMaximo = configuracion.getImporteMaximo();
        form.barajas = configuracion.getBarajas();
        form.bancaPideCon17Blando = configuracion.isBancaPideCon17Blando();
        form.pagoBlackjack = configuracion.getPagoBlackjack();
        return form;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

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

    public Integer getBarajas() {
        return barajas;
    }

    public void setBarajas(Integer barajas) {
        this.barajas = barajas;
    }

    public boolean isBancaPideCon17Blando() {
        return bancaPideCon17Blando;
    }

    public void setBancaPideCon17Blando(boolean bancaPideCon17Blando) {
        this.bancaPideCon17Blando = bancaPideCon17Blando;
    }

    public PagoBlackjack getPagoBlackjack() {
        return pagoBlackjack;
    }

    public void setPagoBlackjack(PagoBlackjack pagoBlackjack) {
        this.pagoBlackjack = pagoBlackjack;
    }
}

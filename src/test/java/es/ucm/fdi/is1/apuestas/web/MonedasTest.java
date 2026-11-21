package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/** Cantidades de monedas en los correos y avisos: como en las páginas. */
class MonedasTest {

    @Test
    void puntoDeMilesYDosDecimales() {
        assertThat(Monedas.texto(new BigDecimal("1234.5"))).isEqualTo("1.234,50");
        assertThat(Monedas.texto(new BigDecimal("1000000"))).isEqualTo("1.000.000,00");
        assertThat(Monedas.texto(BigDecimal.ZERO)).isEqualTo("0,00");
        assertThat(Monedas.texto(new BigDecimal("-50.25"))).isEqualTo("-50,25");
    }

    @Test
    void conMasDecimalesRedondeaComoEnLasPaginas() {
        assertThat(Monedas.texto(new BigDecimal("2.005"))).isEqualTo("2,01");
        assertThat(Monedas.texto(new BigDecimal("2.004"))).isEqualTo("2,00");
    }

    @Test
    void truncadoCortaLosDecimalesQueSobran() {
        assertThat(Monedas.truncado(new BigDecimal("2.009"))).isEqualTo("2,00");
        assertThat(Monedas.truncado(new BigDecimal("1234.567"))).isEqualTo("1.234,56");
        assertThat(Monedas.truncado(new BigDecimal("50"))).isEqualTo("50,00");
    }
}

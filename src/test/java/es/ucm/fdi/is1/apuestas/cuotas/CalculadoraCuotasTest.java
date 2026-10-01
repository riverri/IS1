package es.ucm.fdi.is1.apuestas.cuotas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/** HU-03: cuotas iniciales con margen para la casa. */
class CalculadoraCuotasTest {

    private final CalculadoraCuotas calculadora = new CalculadoraCuotas();

    @Test
    void elFavoritoTieneCuotaMasBaja() {
        Cuotas cuotas = calculadora.calcular(9.0, 5.0, true);

        assertThat(cuotas.local()).isLessThan(cuotas.visitante());
    }

    @Test
    void conEquiposIgualesJugarEnCasaEsVentaja() {
        Cuotas cuotas = calculadora.calcular(7.0, 7.0, true);

        assertThat(cuotas.local()).isLessThan(cuotas.visitante());
    }

    @Test
    void laCasaSiempreTieneMargen() {
        for (double local = 0; local <= 10; local += 0.5) {
            for (double visitante = 0; visitante <= 10; visitante += 0.5) {
                assertThat(calculadora.calcular(local, visitante, true).sumaProbabilidadesImplicitas())
                        .as("fútbol %s-%s", local, visitante).isGreaterThan(BigDecimal.ONE);
                assertThat(calculadora.calcular(local, visitante, false).sumaProbabilidadesImplicitas())
                        .as("sin empate %s-%s", local, visitante).isGreaterThan(BigDecimal.ONE);
            }
        }
    }

    @Test
    void ningunaCuotaBajaDelMinimo() {
        Cuotas cuotas = calculadora.calcular(10.0, 0.0, false);

        assertThat(cuotas.local()).isGreaterThanOrEqualTo(new BigDecimal("1.01"));
        assertThat(cuotas.visitante()).isLessThanOrEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void sinEmpateEnDeportesQueNoLoAdmiten() {
        Cuotas cuotas = calculadora.calcular(8.0, 7.0, false);

        assertThat(cuotas.empate()).isNull();
        assertThat(cuotas.de(Resultado.EMPATE)).isNull();
    }

    @Test
    void lasCuotasTienenDosDecimales() {
        Cuotas cuotas = calculadora.calcular(8.3, 6.1, true);

        assertThat(cuotas.local().scale()).isEqualTo(2);
        assertThat(cuotas.empate().scale()).isEqualTo(2);
        assertThat(cuotas.visitante().scale()).isEqualTo(2);
    }
}

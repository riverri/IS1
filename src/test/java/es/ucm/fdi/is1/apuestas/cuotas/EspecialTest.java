package es.ucm.fdi.is1.apuestas.cuotas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** HU-52: doble oportunidad, más/menos de 2,5 goles y ambos marcan. */
class EspecialTest {

    private final CalculadoraCuotas calculadora = new CalculadoraCuotas(evento -> Map.of());

    @Test
    void dobleOportunidadSeDecideConElResultado() {
        assertThat(Especial.DOBLE_1X.acierta(Resultado.EMPATE, null, null)).isTrue();
        assertThat(Especial.DOBLE_1X.acierta(Resultado.VISITANTE, null, null)).isFalse();
        assertThat(Especial.DOBLE_X2.acierta(Resultado.LOCAL, null, null)).isFalse();
        assertThat(Especial.DOBLE_12.acierta(Resultado.EMPATE, 1, 1)).isFalse();
        assertThat(Especial.DOBLE_12.acierta(Resultado.LOCAL, null, null)).isTrue();
    }

    @Test
    void golesYAmbosMarcanNecesitanElMarcador() {
        assertThat(Especial.MAS_2_5.acierta(Resultado.LOCAL, 2, 1)).isTrue();
        assertThat(Especial.MAS_2_5.acierta(Resultado.LOCAL, 2, 0)).isFalse();
        assertThat(Especial.MENOS_2_5.acierta(Resultado.EMPATE, 1, 1)).isTrue();
        assertThat(Especial.AMBOS_SI.acierta(Resultado.LOCAL, 2, 1)).isTrue();
        assertThat(Especial.AMBOS_NO.acierta(Resultado.LOCAL, 3, 0)).isTrue();
        assertThat(Especial.AMBOS_SI.acierta(Resultado.LOCAL, null, null)).isNull();
        assertThat(Especial.MAS_2_5.acierta(Resultado.LOCAL, null, null)).isNull();
    }

    @Test
    void laCasaTieneMargenEnCadaParDeOpciones() {
        for (double local = 0; local <= 10; local += 1) {
            for (double visitante = 0; visitante <= 10; visitante += 1) {
                Map<Especial, BigDecimal> c = calculadora.especiales(local, visitante);
                assertThat(inversa(c.get(Especial.MAS_2_5)).add(inversa(c.get(Especial.MENOS_2_5))))
                        .as("goles %s-%s", local, visitante).isGreaterThan(BigDecimal.ONE);
                assertThat(inversa(c.get(Especial.AMBOS_SI)).add(inversa(c.get(Especial.AMBOS_NO))))
                        .as("ambos %s-%s", local, visitante).isGreaterThan(BigDecimal.ONE);
                c.values().forEach(cuota -> assertThat(cuota).isBetween(new BigDecimal("1.01"), new BigDecimal("50.00")));
            }
        }
    }

    @Test
    void laDobleOportunidadPagaMenosQueElResultadoSolo() {
        Cuotas cuotas = calculadora.calcular(7.0, 7.0, true);
        Map<Especial, BigDecimal> especiales = calculadora.especiales(7.0, 7.0);

        assertThat(especiales.get(Especial.DOBLE_1X)).isLessThan(cuotas.local());
        assertThat(especiales.get(Especial.DOBLE_X2)).isLessThan(cuotas.visitante());
    }

    @Test
    void conUnFavoritoClaroHayMenosPartidosConGolesDeLosDos() {
        Map<Especial, BigDecimal> igualados = calculadora.especiales(7.0, 7.0);
        Map<Especial, BigDecimal> desigual = calculadora.especiales(10.0, 1.0);

        assertThat(desigual.get(Especial.AMBOS_SI)).isGreaterThan(igualados.get(Especial.AMBOS_SI));
        assertThat(desigual.get(Especial.DOBLE_1X)).isLessThan(igualados.get(Especial.DOBLE_1X));
    }

    private static BigDecimal inversa(BigDecimal cuota) {
        return BigDecimal.ONE.divide(cuota, MathContext.DECIMAL64);
    }
}

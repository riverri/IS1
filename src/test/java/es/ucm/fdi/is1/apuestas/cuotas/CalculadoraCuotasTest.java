package es.ucm.fdi.is1.apuestas.cuotas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** HU-02, HU-03 y fila 3: cuotas con margen para la casa, forma reciente y ajuste por volumen. */
class CalculadoraCuotasTest {

    private final CalculadoraCuotas calculadora = new CalculadoraCuotas(evento -> Map.of());

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

    @Test
    void laBuenaFormaBajaLaCuota() {
        double nivelNormal = CalculadoraCuotas.nivel(7.0, 0);
        double nivelEnRacha = CalculadoraCuotas.nivel(7.0, 2);

        assertThat(nivelEnRacha).isGreaterThan(nivelNormal);
        assertThat(calculadora.calcular(nivelEnRacha, 7.0, true).local())
                .isLessThan(calculadora.calcular(nivelNormal, 7.0, true).local());
    }

    @Test
    void sinDineroApostadoLasCuotasNoCambian() {
        assertThat(calculadora.calcular(8.0, 6.0, true, Map.of())).isEqualTo(calculadora.calcular(8.0, 6.0, true));
    }

    @Test
    void elResultadoConMasDineroBajaDeCuotaYLosDemasSuben() {
        Cuotas sinVolumen = calculadora.calcular(7.0, 7.0, true);
        Cuotas conVolumen = calculadora.calcular(7.0, 7.0, true, Map.of(Resultado.LOCAL, new BigDecimal("2000")));

        assertThat(conVolumen.local()).isLessThan(sinVolumen.local());
        assertThat(conVolumen.empate()).isGreaterThan(sinVolumen.empate());
        assertThat(conVolumen.visitante()).isGreaterThan(sinVolumen.visitante());
    }

    @Test
    void elVolumenTieneUnPesoMaximo() {
        // Aunque todo el dinero vaya al local, el favorito claro sigue siendo el visitante
        Cuotas cuotas = calculadora.calcular(3.0, 9.0, false, Map.of(Resultado.LOCAL, new BigDecimal("1000000")));

        assertThat(cuotas.visitante()).isLessThan(cuotas.local());
    }

    @Test
    void conVolumenLaCasaSigueTeniendoMargen() {
        Map<Resultado, BigDecimal> apostado = Map.of(Resultado.LOCAL, new BigDecimal("500"),
                Resultado.EMPATE, new BigDecimal("30"), Resultado.VISITANTE, new BigDecimal("4000"));
        for (double local = 0; local <= 10; local += 1) {
            assertThat(calculadora.calcular(local, 10 - local, true, apostado).sumaProbabilidadesImplicitas())
                    .isGreaterThan(BigDecimal.ONE);
            assertThat(calculadora.calcular(local, 10 - local, false, apostado).sumaProbabilidadesImplicitas())
                    .isGreaterThan(BigDecimal.ONE);
        }
    }

    @Test
    void elDineroAlEmpateNoCuentaSiNoHayEmpate() {
        assertThat(calculadora.calcular(8.0, 7.0, false, Map.of(Resultado.EMPATE, new BigDecimal("5000"))))
                .isEqualTo(calculadora.calcular(8.0, 7.0, false));
    }
}

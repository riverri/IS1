package es.ucm.fdi.is1.apuestas.casino;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

/** HU-56: reglas y pagos de la ruleta europea (sin base de datos). */
class RuletaTest {

    @Test
    void cadaApuestaGanaConSusNumeros() {
        // CA-56.1
        assertThat(ApuestaRuleta.PLENO.gana(17, 17)).isTrue();
        assertThat(ApuestaRuleta.PLENO.gana(17, 18)).isFalse();
        assertThat(ApuestaRuleta.ROJO.gana(1, null)).isTrue();
        assertThat(ApuestaRuleta.NEGRO.gana(2, null)).isTrue();
        assertThat(ApuestaRuleta.PAR.gana(36, null)).isTrue();
        assertThat(ApuestaRuleta.IMPAR.gana(35, null)).isTrue();
        assertThat(ApuestaRuleta.DOCENA_1.gana(12, null)).isTrue();
        assertThat(ApuestaRuleta.DOCENA_2.gana(13, null)).isTrue();
        assertThat(ApuestaRuleta.DOCENA_3.gana(25, null)).isTrue();
        assertThat(ApuestaRuleta.DOCENA_3.gana(24, null)).isFalse();
    }

    @Test
    void conElCeroSoloGanaElPlenoAlCero() {
        // CA-56.2
        for (ApuestaRuleta apuesta : ApuestaRuleta.values()) {
            if (apuesta != ApuestaRuleta.PLENO) {
                assertThat(apuesta.gana(0, null)).as(apuesta.name()).isFalse();
            }
        }
        assertThat(ApuestaRuleta.PLENO.gana(0, 0)).isTrue();
        assertThat(ApuestaRuleta.color(0)).isEqualTo("verde");
    }

    @Test
    void hayDieciochoRojosYDieciochoNegros() {
        long rojos = java.util.stream.IntStream.rangeClosed(1, 36).filter(n -> ApuestaRuleta.color(n).equals("rojo")).count();
        assertThat(rojos).isEqualTo(18);
    }

    @Test
    void laCasaGanaUnoDeCadaTreintaYSieteALaLarga() {
        // Con 37 números y pagos de 35:1, 1:1 y 2:1, lo esperado devuelto por moneda es 36/37 en todas las apuestas
        for (ApuestaRuleta apuesta : ApuestaRuleta.values()) {
            long ganadores = java.util.stream.IntStream.rangeClosed(0, 36).filter(n -> apuesta.gana(n, 7)).count();
            BigDecimal devueltoEsperado = apuesta.getDevuelve().multiply(BigDecimal.valueOf(ganadores));
            assertThat(devueltoEsperado).as(apuesta.name()).isEqualByComparingTo("36");
        }
    }

    @Test
    void conMuchasTiradasSaleCadaNumeroMasOMenosLasMismasVeces() {
        // El servidor gira con nextInt(37): con una semilla fija, en 37.000 tiradas cada número sale cerca de 1.000 veces
        SplittableRandom azar = new SplittableRandom(2026);
        int[] veces = new int[37];
        for (int i = 0; i < 37_000; i++) {
            veces[azar.nextInt(37)]++;
        }
        for (int numero = 0; numero <= 36; numero++) {
            assertThat(veces[numero]).as("número " + numero).isBetween(850, 1150);
        }
    }
}

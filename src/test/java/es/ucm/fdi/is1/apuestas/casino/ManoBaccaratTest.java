package es.ucm.fdi.is1.apuestas.casino;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

/**
 * HU-74: reglas del baccarat (sin base de datos). En {@code conCartas} las cartas salen en orden: Jugador, Banca,
 * Jugador, Banca y después las terceras.
 */
class ManoBaccaratTest {

    private static Carta c(int rango) {
        return new Carta(rango, 'P');
    }

    private static ManoBaccarat mano(int... rangos) {
        return ManoBaccarat.conCartas(java.util.Arrays.stream(rangos).mapToObj(ManoBaccaratTest::c).toList());
    }

    @Test
    void lasFigurasValenCeroYSoloCuentanLasUnidades() {
        assertThat(ManoBaccarat.valor(List.of(c(7), c(8)))).isEqualTo(5);
        assertThat(ManoBaccarat.valor(List.of(c(13), c(10), c(1)))).isEqualTo(1);
        assertThat(ManoBaccarat.valor(List.of(c(9), c(9), c(9)))).isEqualTo(7);
    }

    @Test
    void conUnNaturalNadiePideMas() {
        // Jugador 4 + 4 = 8 (natural); Banca 2 + 3 = 5 tendría que pedir, pero no lo hace
        ManoBaccarat mano = mano(4, 2, 4, 3, 9, 9);

        assertThat(mano.getJugador()).hasSize(2);
        assertThat(mano.getBanca()).hasSize(2);
        assertThat(mano.ganador()).isEqualTo(ApuestaBaccarat.JUGADOR);
    }

    @Test
    void elJugadorPideConCincoOMenosYSePlantaConSeisOSiete() {
        // Jugador 2 + 3 = 5: pide (sale un 2 → 7). Banca 3 + 4 = 7: se planta siempre
        ManoBaccarat pide = mano(2, 3, 3, 4, 2);
        assertThat(pide.getJugador()).hasSize(3);
        assertThat(pide.getBanca()).hasSize(2);
        assertThat(pide.ganador()).isEqualTo(ApuestaBaccarat.EMPATE);

        // Jugador 3 + 3 = 6: se planta. Banca 1 + 4 = 5: pide porque el Jugador se plantó (sale un 13 → 5)
        ManoBaccarat planta = mano(3, 1, 3, 4, 13);
        assertThat(planta.getJugador()).hasSize(2);
        assertThat(planta.getBanca()).hasSize(3);
        assertThat(planta.ganador()).isEqualTo(ApuestaBaccarat.JUGADOR);
    }

    @Test
    void tablaDeLaTerceraCartaDeLaBanca() {
        assertThat(ManoBaccarat.bancaPide(2, 8)).isTrue();
        assertThat(ManoBaccarat.bancaPide(3, 8)).isFalse();
        assertThat(ManoBaccarat.bancaPide(3, 9)).isTrue();
        assertThat(ManoBaccarat.bancaPide(4, 1)).isFalse();
        assertThat(ManoBaccarat.bancaPide(4, 7)).isTrue();
        assertThat(ManoBaccarat.bancaPide(5, 3)).isFalse();
        assertThat(ManoBaccarat.bancaPide(5, 4)).isTrue();
        assertThat(ManoBaccarat.bancaPide(6, 5)).isFalse();
        assertThat(ManoBaccarat.bancaPide(6, 6)).isTrue();
        assertThat(ManoBaccarat.bancaPide(7, 6)).isFalse();
        // Si el Jugador se plantó: pide con 5 o menos
        assertThat(ManoBaccarat.bancaPide(5, null)).isTrue();
        assertThat(ManoBaccarat.bancaPide(6, null)).isFalse();
    }

    @Test
    void conEmpateSeDevuelveLaApuestaAJugadorYABanca() {
        // CA-74.3
        assertThat(ApuestaBaccarat.JUGADOR.devuelveSiGana(ApuestaBaccarat.EMPATE)).isEqualByComparingTo("1");
        assertThat(ApuestaBaccarat.BANCA.devuelveSiGana(ApuestaBaccarat.EMPATE)).isEqualByComparingTo("1");
        assertThat(ApuestaBaccarat.EMPATE.devuelveSiGana(ApuestaBaccarat.EMPATE)).isEqualByComparingTo("9");
        assertThat(ApuestaBaccarat.BANCA.devuelveSiGana(ApuestaBaccarat.BANCA)).isEqualByComparingTo("1.95");
        assertThat(ApuestaBaccarat.BANCA.devuelveSiGana(ApuestaBaccarat.JUGADOR)).isEqualByComparingTo("0");
    }

    /**
     * Con muchas manos, cada apuesta devuelve de media menos de lo apostado, cerca de la ventaja de la casa de las
     * tablas (Banca 1,06 %, Jugador 1,24 %). Con semilla fija siempre sale lo mismo.
     */
    @Test
    void laCasaGanaALaLarga() {
        SplittableRandom azar = new SplittableRandom(7);
        int manos = 100_000;
        double banca = 0;
        double jugador = 0;
        for (int i = 0; i < manos; i++) {
            ApuestaBaccarat ganador = ManoBaccarat.jugar(azar).ganador();
            banca += ApuestaBaccarat.BANCA.devuelveSiGana(ganador).doubleValue() - 1;
            jugador += ApuestaBaccarat.JUGADOR.devuelveSiGana(ganador).doubleValue() - 1;
        }
        assertThat(banca / manos).isBetween(-0.03, 0.0);
        assertThat(jugador / manos).isBetween(-0.03, 0.0);
    }
}

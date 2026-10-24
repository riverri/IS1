package es.ucm.fdi.is1.apuestas.casino;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

/** HU-57: reglas del blackjack (sin base de datos). En {@code conCartas} el mazo se reparte desde el final. */
class ManoBlackjackTest {

    private static Carta c(int rango) {
        return new Carta(rango, 'P');
    }

    @Test
    void losAsesValenOnceMientrasNoSePase() {
        assertThat(ManoBlackjack.valor(List.of(c(1), c(6)))).isEqualTo(17);
        assertThat(ManoBlackjack.esBlanda(List.of(c(1), c(6)))).isTrue();
        assertThat(ManoBlackjack.valor(List.of(c(1), c(6), c(9)))).isEqualTo(16);
        assertThat(ManoBlackjack.valor(List.of(c(1), c(1), c(9)))).isEqualTo(21);
        assertThat(ManoBlackjack.valor(List.of(c(13), c(12)))).isEqualTo(20);
    }

    @Test
    void unBlackjackAlRepartirTerminaLaMano() {
        // CA-57.2
        ManoBlackjack mano = ManoBlackjack.conCartas(List.of(c(1), c(13)), List.of(c(10), c(9)), List.of(c(2)));

        assertThat(mano.isTerminada()).isTrue();
        assertThat(mano.resultado()).isEqualTo(ResultadoMano.BLACKJACK);
    }

    @Test
    void siLosDosTienenBlackjackEsEmpate() {
        ManoBlackjack mano = ManoBlackjack.conCartas(List.of(c(1), c(13)), List.of(c(1), c(10)), List.of());

        assertThat(mano.resultado()).isEqualTo(ResultadoMano.EMPATE);
    }

    @Test
    void siSePasaDeVeintiunoPierdeYLaBancaNoJuega() {
        // CA-57.4
        ManoBlackjack mano = ManoBlackjack.conCartas(List.of(c(10), c(6)), List.of(c(10), c(6)), List.of(c(5), c(9)));

        mano.pedir(false);

        assertThat(mano.isTerminada()).isTrue();
        assertThat(mano.resultado()).isEqualTo(ResultadoMano.PIERDE);
        assertThat(mano.getBanca()).hasSize(2);
    }

    @Test
    void alPlantarseLaBancaPideHastaDiecisiete() {
        // Banca 10 + 6 = 16: pide un 5 (21). Tú 20: pierdes
        ManoBlackjack mano = ManoBlackjack.conCartas(List.of(c(10), c(13)), List.of(c(10), c(6)), List.of(c(5)));

        mano.plantarse(false);

        assertThat(ManoBlackjack.valor(mano.getBanca())).isEqualTo(21);
        assertThat(mano.resultado()).isEqualTo(ResultadoMano.PIERDE);
    }

    @Test
    void conDiecisieteBlandoLaBancaSoloPideSiLaReglaLoDice() {
        // Banca A + 6 = 17 blando; la siguiente carta del mazo es un 3
        ManoBlackjack sinPedir = ManoBlackjack.conCartas(List.of(c(10), c(8)), List.of(c(1), c(6)), List.of(c(3)));
        ManoBlackjack pidiendo = ManoBlackjack.conCartas(List.of(c(10), c(8)), List.of(c(1), c(6)), List.of(c(3)));

        sinPedir.plantarse(false);
        pidiendo.plantarse(true);

        assertThat(ManoBlackjack.valor(sinPedir.getBanca())).isEqualTo(17);
        assertThat(sinPedir.resultado()).isEqualTo(ResultadoMano.GANA);
        assertThat(ManoBlackjack.valor(pidiendo.getBanca())).isEqualTo(20);
        assertThat(pidiendo.resultado()).isEqualTo(ResultadoMano.PIERDE);
    }

    @Test
    void siLaBancaSePasaGanaElJugador() {
        ManoBlackjack mano = ManoBlackjack.conCartas(List.of(c(10), c(2)), List.of(c(10), c(6)), List.of(c(13)));

        mano.plantarse(false);

        assertThat(mano.resultado()).isEqualTo(ResultadoMano.GANA);
    }

    @Test
    void doblarDaUnaSolaCartaYTerminaLaMano() {
        // CA-57.3: tú 6 + 5, doblas y recibes un 10 (21); la banca 10 + 7 se planta
        ManoBlackjack mano = ManoBlackjack.conCartas(List.of(c(6), c(5)), List.of(c(10), c(7)), List.of(c(10)));

        mano.doblar(false);

        assertThat(mano.isDoblada()).isTrue();
        assertThat(mano.getJugador()).hasSize(3);
        assertThat(mano.resultado()).isEqualTo(ResultadoMano.GANA);
    }

    @Test
    void soloSeDoblaConLasDosPrimerasCartasYNoSeJuegaUnaManoTerminada() {
        ManoBlackjack mano = ManoBlackjack.conCartas(List.of(c(2), c(3)), List.of(c(10), c(7)), List.of(c(9), c(4)));
        mano.pedir(false);

        assertThatThrownBy(() -> mano.doblar(false)).isInstanceOf(IllegalStateException.class);
        mano.plantarse(false);
        assertThatThrownBy(() -> mano.pedir(false)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya ha terminado");
    }

    @Test
    void laManoSeGuardaYSeRecuperaTalCual() {
        // CA-57.5: el estado entre peticiones está en la partida, con la baraja que queda
        ManoBlackjack mano = ManoBlackjack.repartir(6, new SplittableRandom(7));
        ManoBlackjack leida = ManoBlackjack.leer(mano.guardar());

        assertThat(leida.getJugador()).isEqualTo(mano.getJugador());
        assertThat(leida.getBanca()).isEqualTo(mano.getBanca());
        assertThat(leida.guardar()).isEqualTo(mano.guardar());
        // 6 barajas de 52 cartas menos las 4 repartidas, y cabe en la columna
        assertThat(mano.guardar().split("\\|")[0].split(",")).hasSize(6 * 52 - 4);
        assertThat(ManoBlackjack.repartir(8, new SplittableRandom(1)).guardar().length()).isLessThan(2000);
    }
}

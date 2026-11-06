package es.ucm.fdi.is1.apuestas.casino;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lo que ve el jugador de su mano de blackjack (CA-57.1). Mientras la mano sigue, de la banca solo va su primera carta:
 * la otra y el resto de la baraja no salen del servidor, así no se pueden mirar en el código de la página.
 *
 * @param cartasBanca  la primera carta si la mano sigue; todas si ha terminado
 * @param valorBanca   valor de las cartas que se ven
 * @param resultado    null mientras la mano sigue
 */
public record MesaBlackjack(Long partidaId, List<Carta> cartasJugador, int valorJugador, List<Carta> cartasBanca,
                            int valorBanca, boolean terminada, boolean puedeDoblar, BigDecimal apostado,
                            BigDecimal premio, ResultadoMano resultado) {

    static MesaBlackjack de(Partida partida) {
        ManoBlackjack mano = partida.mano();
        List<Carta> banca = mano.isTerminada() ? mano.getBanca() : List.of(mano.getBanca().get(0));
        return new MesaBlackjack(partida.getId(), mano.getJugador(), ManoBlackjack.valor(mano.getJugador()), banca,
                ManoBlackjack.valor(banca), mano.isTerminada(), mano.isPuedeDoblar(), partida.getApostado(),
                partida.getPremio(), mano.isTerminada() ? mano.resultado() : null);
    }
}

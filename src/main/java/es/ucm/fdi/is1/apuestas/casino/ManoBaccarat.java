package es.ucm.fdi.is1.apuestas.casino;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Una mano de baccarat Punto Banco (HU-74). A diferencia del blackjack, el jugador no decide nada: solo apuesta a
 * quién gana (Jugador, Banca o Empate) y las cartas se reparten solas según la tabla fija de la tercera carta. Por eso
 * la mano se juega entera en una sola petición, como una tirada de ruleta.
 *
 * <p>Valor de una mano: la suma de las cartas quedándose con las unidades (7 + 8 = 15 → 5). Los ases valen 1 y el 10
 * y las figuras, 0. Gana quien se acerque más a 9.
 *
 * <p>Reglas, en orden:
 * <ol>
 *     <li>Dos cartas a cada lado, alternando: Jugador, Banca, Jugador, Banca.</li>
 *     <li>Si alguno tiene 8 o 9 ("natural"), nadie pide más.</li>
 *     <li>El Jugador pide una tercera carta con 0 a 5 y se planta con 6 o 7.</li>
 *     <li>Si el Jugador se plantó, la Banca pide con 0 a 5. Si el Jugador pidió, la Banca decide según su total y el
 *     valor de esa tercera carta (ver {@link #bancaPide}).</li>
 * </ol>
 */
public final class ManoBaccarat {

    /** En los casinos se juega con 8 barajas: con tantas, la ventaja de la casa es la de la tabla de la página. */
    static final int BARAJAS = 8;

    private static final char[] PALOS = {'P', 'C', 'D', 'T'};

    private final List<Carta> jugador = new ArrayList<>();
    private final List<Carta> banca = new ArrayList<>();

    private ManoBaccarat() {
    }

    /** Baraja 8 barajas con el azar del servidor y juega la mano entera. */
    public static ManoBaccarat jugar(RandomGenerator azar) {
        List<Carta> mazo = new ArrayList<>();
        for (int b = 0; b < BARAJAS; b++) {
            for (char palo : PALOS) {
                for (int rango = 1; rango <= 13; rango++) {
                    mazo.add(new Carta(rango, palo));
                }
            }
        }
        Collections.shuffle(mazo, azar);
        return conCartas(mazo);
    }

    /**
     * Juega la mano sacando las cartas desde el principio de la lista. Para las pruebas: así se sabe qué sale.
     *
     * @param mazo cartas en el orden en que se reparten (Jugador, Banca, Jugador, Banca y las terceras)
     */
    static ManoBaccarat conCartas(List<Carta> mazo) {
        ManoBaccarat mano = new ManoBaccarat();
        int siguiente = 0;
        mano.jugador.add(mazo.get(siguiente++));
        mano.banca.add(mazo.get(siguiente++));
        mano.jugador.add(mazo.get(siguiente++));
        mano.banca.add(mazo.get(siguiente++));

        int totalJugador = valor(mano.jugador);
        int totalBanca = valor(mano.banca);
        // Natural: con 8 o 9 en cualquiera de los dos lados se acaba la mano
        if (totalJugador >= 8 || totalBanca >= 8) {
            return mano;
        }
        Integer terceraJugador = null;
        if (totalJugador <= 5) {
            Carta tercera = mazo.get(siguiente++);
            mano.jugador.add(tercera);
            terceraJugador = valor(tercera);
        }
        if (bancaPide(totalBanca, terceraJugador)) {
            mano.banca.add(mazo.get(siguiente));
        }
        return mano;
    }

    /**
     * Tabla de la tercera carta de la Banca.
     *
     * @param totalBanca     total de la Banca con sus dos cartas (0 a 7: con 8 o 9 ya no se llega aquí)
     * @param terceraJugador valor de la tercera carta del Jugador; null si el Jugador se plantó
     */
    static boolean bancaPide(int totalBanca, Integer terceraJugador) {
        if (terceraJugador == null) {
            return totalBanca <= 5;
        }
        int t = terceraJugador;
        return switch (totalBanca) {
            case 0, 1, 2 -> true;
            case 3 -> t != 8;
            case 4 -> t >= 2 && t <= 7;
            case 5 -> t >= 4 && t <= 7;
            case 6 -> t == 6 || t == 7;
            // 7: siempre se planta
            default -> false;
        };
    }

    /** Valor de una carta en el baccarat: el as 1, del 2 al 9 lo que marcan, el 10 y las figuras 0. */
    static int valor(Carta carta) {
        return carta.rango() >= 10 ? 0 : carta.rango();
    }

    /** Valor de una mano: la suma quedándose con las unidades (de 0 a 9). */
    static int valor(List<Carta> cartas) {
        return cartas.stream().mapToInt(ManoBaccarat::valor).sum() % 10;
    }

    /** Quién gana la mano. */
    public ApuestaBaccarat ganador() {
        int j = getValorJugador();
        int b = getValorBanca();
        if (j == b) {
            return ApuestaBaccarat.EMPATE;
        }
        return j > b ? ApuestaBaccarat.JUGADOR : ApuestaBaccarat.BANCA;
    }

    public List<Carta> getJugador() {
        return Collections.unmodifiableList(jugador);
    }

    public List<Carta> getBanca() {
        return Collections.unmodifiableList(banca);
    }

    public int getValorJugador() {
        return valor(jugador);
    }

    public int getValorBanca() {
        return valor(banca);
    }
}

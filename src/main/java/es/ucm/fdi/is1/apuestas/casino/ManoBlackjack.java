package es.ucm.fdi.is1.apuestas.casino;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

/**
 * Reglas de una mano de blackjack (HU-57), sin base de datos ni Spring: así se prueban solas.
 *
 * <p>Se reparten dos cartas al jugador y dos a la banca, que enseña solo la primera. El jugador pide carta, se planta
 * o dobla (una sola carta más y apuesta doble). Después juega la banca: pide hasta tener 17 o más y, si la
 * configuración lo dice, también con un 17 "blando" (con un as contado como 11). Si alguien tiene blackjack al
 * repartir, la mano termina en el acto. No se puede dividir ni asegurar (fuera del alcance de HU-57).
 *
 * <p>La baraja barajada se guarda entera en la partida ({@link #guardar}): las cartas siguientes ya están decididas
 * en el servidor, y el navegador solo recibe las que se ven ({@link MesaBlackjack}).
 */
public class ManoBlackjack {

    private static final char[] PALOS = {'P', 'C', 'D', 'T'};

    private final List<Carta> mazo;
    private final List<Carta> jugador;
    private final List<Carta> banca;
    private boolean doblada;
    private boolean terminada;

    private ManoBlackjack(List<Carta> mazo, List<Carta> jugador, List<Carta> banca, boolean doblada, boolean terminada) {
        this.mazo = mazo;
        this.jugador = jugador;
        this.banca = banca;
        this.doblada = doblada;
        this.terminada = terminada;
    }

    /** Baraja {@code barajas} barajas de 52 cartas con el azar del servidor y reparte. */
    public static ManoBlackjack repartir(int barajas, RandomGenerator azar) {
        List<Carta> mazo = new ArrayList<>();
        for (int b = 0; b < barajas; b++) {
            for (char palo : PALOS) {
                for (int rango = 1; rango <= 13; rango++) {
                    mazo.add(new Carta(rango, palo));
                }
            }
        }
        Collections.shuffle(mazo, azar);
        ManoBlackjack mano = new ManoBlackjack(mazo, new ArrayList<>(), new ArrayList<>(), false, false);
        // Una carta al jugador, una a la banca, otra al jugador y otra a la banca, como en la mesa
        mano.jugador.add(mano.sacar());
        mano.banca.add(mano.sacar());
        mano.jugador.add(mano.sacar());
        mano.banca.add(mano.sacar());
        // Con un blackjack en cualquiera de los dos lados no hay nada que decidir
        if (esBlackjack(mano.jugador) || esBlackjack(mano.banca)) {
            mano.terminada = true;
        }
        return mano;
    }

    /** Una carta más; si pasa de 21, la mano termina y la banca ya no juega. Con 21 justos se planta solo. */
    public void pedir(boolean bancaPideCon17Blando) {
        comprobarEnCurso();
        jugador.add(sacar());
        if (valor(jugador) > 21) {
            terminada = true;
        } else if (valor(jugador) == 21) {
            // Con 21 no tiene sentido pedir más: juega la banca
            jugarBanca(bancaPideCon17Blando);
            terminada = true;
        }
    }

    /** El jugador se planta y juega la banca. */
    public void plantarse(boolean bancaPideCon17Blando) {
        comprobarEnCurso();
        jugarBanca(bancaPideCon17Blando);
        terminada = true;
    }

    /**
     * Dobla la apuesta: recibe una sola carta más y la mano termina (CA-57.3). Solo con las dos primeras cartas.
     *
     * @throws IllegalStateException si ya ha pedido carta o la mano ha terminado
     */
    public void doblar(boolean bancaPideCon17Blando) {
        comprobarEnCurso();
        if (jugador.size() != 2) {
            throw new IllegalStateException("Solo se puede doblar con las dos primeras cartas");
        }
        doblada = true;
        jugador.add(sacar());
        if (valor(jugador) <= 21) {
            jugarBanca(bancaPideCon17Blando);
        }
        terminada = true;
    }

    /** Pide hasta 17; con un 17 blando, solo si la regla de la mesa lo dice. */
    private void jugarBanca(boolean pideCon17Blando) {
        while (valor(banca) < 17 || (valor(banca) == 17 && esBlanda(banca) && pideCon17Blando)) {
            banca.add(sacar());
        }
    }

    /** Resultado de la mano terminada. */
    public ResultadoMano resultado() {
        if (!terminada) {
            throw new IllegalStateException("La mano aún no ha terminado");
        }
        int tuyo = valor(jugador);
        int suyo = valor(banca);
        if (tuyo > 21) {
            return ResultadoMano.PIERDE;
        }
        boolean blackjackTuyo = esBlackjack(jugador);
        boolean blackjackSuyo = esBlackjack(banca);
        if (blackjackTuyo || blackjackSuyo) {
            return blackjackTuyo && blackjackSuyo ? ResultadoMano.EMPATE
                    : blackjackTuyo ? ResultadoMano.BLACKJACK : ResultadoMano.PIERDE;
        }
        if (suyo > 21 || tuyo > suyo) {
            return ResultadoMano.GANA;
        }
        return tuyo == suyo ? ResultadoMano.EMPATE : ResultadoMano.PIERDE;
    }

    /**
     * Valor de unas cartas: los ases valen 11 mientras no se pase de 21; si se pasa, valen 1.
     * Ejemplo: A + 6 = 17 (blando); A + 6 + 9 = 16, porque el as pasa a valer 1.
     */
    public static int valor(List<Carta> cartas) {
        int total = cartas.stream().mapToInt(Carta::valor).sum();
        boolean hayAs = cartas.stream().anyMatch(Carta::isAs);
        // Un as como 11 suma 10 más que como 1; nunca puede haber dos ases a 11 (serían 22)
        return hayAs && total + 10 <= 21 ? total + 10 : total;
    }

    /** "Blanda": tiene un as contado como 11. */
    static boolean esBlanda(List<Carta> cartas) {
        int total = cartas.stream().mapToInt(Carta::valor).sum();
        return cartas.stream().anyMatch(Carta::isAs) && total + 10 <= 21;
    }

    static boolean esBlackjack(List<Carta> cartas) {
        return cartas.size() == 2 && valor(cartas) == 21;
    }

    private Carta sacar() {
        return mazo.remove(mazo.size() - 1);
    }

    private void comprobarEnCurso() {
        if (terminada) {
            throw new IllegalStateException("Esta mano ya ha terminado");
        }
    }

    /** Texto para guardar la mano en la partida: "mazo|jugador|banca|doblada|terminada". */
    String guardar() {
        return codigos(mazo) + "|" + codigos(jugador) + "|" + codigos(banca) + "|" + doblada + "|" + terminada;
    }

    static ManoBlackjack leer(String guardada) {
        // limit -1: conserva los trozos vacíos (un mazo vacío no se pierde)
        String[] partes = guardada.split("\\|", -1);
        return new ManoBlackjack(cartas(partes[0]), cartas(partes[1]), cartas(partes[2]),
                Boolean.parseBoolean(partes[3]), Boolean.parseBoolean(partes[4]));
    }

    private static String codigos(List<Carta> cartas) {
        return cartas.stream().map(Carta::codigo).collect(Collectors.joining(","));
    }

    private static List<Carta> cartas(String codigos) {
        if (codigos.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.stream(codigos.split(",")).map(Carta::deCodigo).toList());
    }

    /** Solo para las pruebas: una mano con el mazo ya ordenado (la última carta de la lista sale primero). */
    static ManoBlackjack conCartas(List<Carta> jugador, List<Carta> banca, List<Carta> mazo) {
        ManoBlackjack mano = new ManoBlackjack(new ArrayList<>(mazo), new ArrayList<>(jugador), new ArrayList<>(banca),
                false, false);
        if (esBlackjack(mano.jugador) || esBlackjack(mano.banca)) {
            mano.terminada = true;
        }
        return mano;
    }

    public List<Carta> getJugador() {
        return Collections.unmodifiableList(jugador);
    }

    /** Todas las de la banca; mientras la mano sigue, la vista solo debe enseñar la primera ({@link MesaBlackjack}). */
    List<Carta> getBanca() {
        return Collections.unmodifiableList(banca);
    }

    public boolean isDoblada() {
        return doblada;
    }

    public boolean isTerminada() {
        return terminada;
    }

    /** Se puede doblar mientras tenga sus dos primeras cartas y la mano siga. */
    public boolean isPuedeDoblar() {
        return !terminada && jugador.size() == 2;
    }
}

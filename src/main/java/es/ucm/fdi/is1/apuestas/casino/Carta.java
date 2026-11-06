package es.ucm.fdi.is1.apuestas.casino;

/**
 * Una carta de la baraja francesa para el blackjack (HU-57).
 *
 * @param rango 1 (as) a 13 (rey)
 * @param palo  'P' picas, 'C' corazones, 'D' diamantes, 'T' tréboles
 */
public record Carta(int rango, char palo) {

    /** Valor en el blackjack: las figuras valen 10 y el as 1 (si conviene, {@link ManoBlackjack} lo cuenta como 11). */
    public int valor() {
        return Math.min(rango, 10);
    }

    public boolean isAs() {
        return rango == 1;
    }

    /** Corazones y diamantes son rojos, para pintarlas. */
    public boolean isRoja() {
        return palo == 'C' || palo == 'D';
    }

    /** "A♠", "10♥", "K♦"… */
    public String getTexto() {
        String figura = switch (rango) {
            case 1 -> "A";
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            default -> String.valueOf(rango);
        };
        String simbolo = switch (palo) {
            case 'P' -> "♠";
            case 'C' -> "♥";
            case 'D' -> "♦";
            default -> "♣";
        };
        return figura + simbolo;
    }

    /** Forma compacta para guardarla en la partida: rango y palo, por ejemplo "1P" o "12C". */
    String codigo() {
        return rango + String.valueOf(palo);
    }

    static Carta deCodigo(String codigo) {
        return new Carta(Integer.parseInt(codigo.substring(0, codigo.length() - 1)), codigo.charAt(codigo.length() - 1));
    }
}

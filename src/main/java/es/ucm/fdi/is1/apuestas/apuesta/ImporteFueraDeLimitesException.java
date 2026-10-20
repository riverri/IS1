package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

/**
 * El importe de la apuesta no respeta los límites que ha fijado el creador (HU-07). La lanza
 * {@link Limites#comprobarImporte}; el mensaje indica el rango permitido (CA-07.1).
 */
public class ImporteFueraDeLimitesException extends IllegalArgumentException {

    public ImporteFueraDeLimitesException(BigDecimal minimo, BigDecimal maximo) {
        super("El importe debe estar entre " + texto(minimo) + " y " + texto(maximo) + " monedas");
    }

    /** "1" en lugar de "1.00" y "2,5" en lugar de "2.50": formato español sin ceros de sobra. */
    private static String texto(BigDecimal cantidad) {
        return cantidad.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}

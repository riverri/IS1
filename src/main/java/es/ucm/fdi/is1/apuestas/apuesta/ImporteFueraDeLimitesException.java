package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

/** El importe de la apuesta no respeta los límites que ha fijado el creador (HU-07). */
public class ImporteFueraDeLimitesException extends IllegalArgumentException {

    public ImporteFueraDeLimitesException(BigDecimal minimo, BigDecimal maximo) {
        super("El importe debe estar entre " + texto(minimo) + " y " + texto(maximo) + " monedas");
    }

    private static String texto(BigDecimal cantidad) {
        return cantidad.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}

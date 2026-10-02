package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

/**
 * Cuánto se ha apostado a una opción de un evento y en cuántas apuestas (HU-06): un resultado (1X2)
 * o uno de los otros tipos de apuesta (HU-52), con su cuota actual.
 */
public record VolumenResultado(String simbolo, String descripcion, BigDecimal cuota, BigDecimal importe,
                               long apuestas) {
}

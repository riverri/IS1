package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

/**
 * Cuánto se ha apostado a una opción de un evento y en cuántas apuestas (HU-06): un resultado (1X2)
 * o uno de los otros tipos de apuesta (HU-52), con su cuota actual. Lo calcula {@link ResolucionService#volumen}.
 *
 * @param cuota    cuota actual; null si la opción ya no se ofrece pero tiene apuestas
 * @param importe  suma de los importes completos de las apuestas (también de las combinadas)
 * @param apuestas número de apuestas que incluyen esta opción
 */
public record VolumenResultado(String simbolo, String descripcion, BigDecimal cuota, BigDecimal importe,
                               long apuestas) {
}

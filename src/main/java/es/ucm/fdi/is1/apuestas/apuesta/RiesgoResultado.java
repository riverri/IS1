package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

/**
 * Lo que pagaría la casa si el evento acaba con un resultado (HU-76). Lo calcula {@link RiesgoService}.
 *
 * @param pago      ganancias de todas las apuestas que se cobrarían con ese resultado
 * @param saldoCasa lo apostado al evento menos el pago: negativo si la casa pierde dinero
 */
public record RiesgoResultado(String simbolo, String descripcion, BigDecimal pago, BigDecimal saldoCasa) {
}

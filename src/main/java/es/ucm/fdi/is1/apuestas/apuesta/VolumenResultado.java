package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

/** Cuánto se ha apostado a un resultado de un evento y en cuántas apuestas (HU-06). */
public record VolumenResultado(Resultado resultado, BigDecimal importe, long apuestas) {
}

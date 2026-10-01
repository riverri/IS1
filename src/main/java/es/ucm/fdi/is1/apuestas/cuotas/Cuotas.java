package es.ucm.fdi.is1.apuestas.cuotas;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Cuotas decimales de un evento. {@code empate} es null en deportes sin empate.
 */
public record Cuotas(BigDecimal local, BigDecimal empate, BigDecimal visitante) {

    /** Devuelve la cuota de un resultado, o null si ese resultado no es posible. */
    public BigDecimal de(Resultado resultado) {
        return switch (resultado) {
            case LOCAL -> local;
            case EMPATE -> empate;
            case VISITANTE -> visitante;
        };
    }

    /** Suma de probabilidades implícitas (1/cuota). Mayor que 1 significa margen para la casa. */
    public BigDecimal sumaProbabilidadesImplicitas() {
        BigDecimal suma = BigDecimal.ONE.divide(local, MathContext.DECIMAL64)
                .add(BigDecimal.ONE.divide(visitante, MathContext.DECIMAL64));
        if (empate != null) {
            suma = suma.add(BigDecimal.ONE.divide(empate, MathContext.DECIMAL64));
        }
        return suma;
    }
}

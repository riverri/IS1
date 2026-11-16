package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Riesgo de la casa en un evento (HU-76): lo apostado y lo que quedaría con cada resultado.
 *
 * @param apostado   suma de los importes de las apuestas al evento (las combinadas enteras)
 * @param resultados una fila por resultado posible (1, X si hay empate, 2)
 * @param limite     pérdida a partir de la cual se avisa ({@link Limites#getAlertaRiesgo})
 */
public record RiesgoEvento(BigDecimal apostado, List<RiesgoResultado> resultados, BigDecimal limite) {

    /** Lo que le queda a la casa con el peor resultado. */
    public BigDecimal getPeorSaldo() {
        return resultados.stream().map(RiesgoResultado::saldoCasa).min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    /** Si en el peor caso la casa pierde más que el límite (CA-76.2). */
    public boolean isAlerta() {
        return getPeorSaldo().compareTo(limite.negate()) < 0;
    }

    public boolean isSinApuestas() {
        return apostado.signum() == 0;
    }
}

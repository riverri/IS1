package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Lo que se ha apostado, pagado y ganado la casa (HU-54), en total y desglosado.
 * Lo construye {@link CuentasCasaService} y lo pinta el panel de la casa.
 *
 * @param porTipo       simples 1X2, simples de doble oportunidad y goles, combinadas y a largo plazo
 * @param porDeporte    solo las simples de partidos, por deporte
 * @param jugadores     usuarios distintos que han apostado
 * @param margenTeorico margen que debería quedarse la casa según la fórmula de cuotas, en %
 */
public record CuentasCasa(Fila total, List<Fila> porTipo, List<Fila> porDeporte, long jugadores,
                          BigDecimal margenTeorico) {

    /**
     * Una fila del panel. {@code apostado} y {@code pagado} son de las apuestas ya cerradas (ganadas, perdidas
     * o anuladas); {@code enJuego} es lo apostado en las activas.
     */
    public record Fila(String nombre, int apuestas, BigDecimal enJuego, BigDecimal apostado, BigDecimal pagado) {

        /** Suma las apuestas de una fila; las activas van aparte porque aún no se sabe qué se pagará. */
        static Fila de(String nombre, List<Apuesta> apuestas) {
            BigDecimal enJuego = BigDecimal.ZERO;
            BigDecimal apostado = BigDecimal.ZERO;
            BigDecimal pagado = BigDecimal.ZERO;
            for (Apuesta a : apuestas) {
                // pagado incluye el importe devuelto en las anuladas: en ellas la casa ni gana ni pierde
                if (a.getEstado() == EstadoApuesta.ACTIVA) {
                    enJuego = enJuego.add(a.getImporte());
                } else {
                    apostado = apostado.add(a.getImporte());
                    pagado = pagado.add(a.getPagado());
                }
            }
            return new Fila(nombre, apuestas.size(), enJuego, apostado, pagado);
        }

        /** Lo que se queda la casa de las apuestas cerradas: apostado − pagado. */
        public BigDecimal getBeneficio() {
            return apostado.subtract(pagado);
        }

        /**
         * Margen real: beneficio / apostado, en tanto por ciento con un decimal. Se compara con el margen
         * teórico (CA-54.1). Null si aún no hay nada cerrado.
         */
        public BigDecimal getMargen() {
            if (apostado.signum() == 0) {
                return null;
            }
            return getBeneficio().multiply(BigDecimal.valueOf(100)).divide(apostado, 1, RoundingMode.HALF_UP);
        }
    }
}

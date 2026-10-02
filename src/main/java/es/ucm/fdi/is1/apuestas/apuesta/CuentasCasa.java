package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Lo que se ha apostado, pagado y ganado la casa (HU-54), en total y desglosado.
 *
 * @param porTipo    simples, combinadas y a largo plazo
 * @param porDeporte solo las simples de partidos, por deporte
 */
public record CuentasCasa(Fila total, List<Fila> porTipo, List<Fila> porDeporte, long jugadores,
                          BigDecimal margenTeorico) {

    /**
     * Una fila del panel. {@code apostado} y {@code pagado} son de las apuestas ya cerradas (ganadas, perdidas
     * o anuladas); {@code enJuego} es lo apostado en las activas.
     */
    public record Fila(String nombre, int apuestas, BigDecimal enJuego, BigDecimal apostado, BigDecimal pagado) {

        static Fila de(String nombre, List<Apuesta> apuestas) {
            BigDecimal enJuego = BigDecimal.ZERO;
            BigDecimal apostado = BigDecimal.ZERO;
            BigDecimal pagado = BigDecimal.ZERO;
            for (Apuesta a : apuestas) {
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

        /** Beneficio / apostado, en tanto por ciento con un decimal. Null si aún no hay nada cerrado. */
        public BigDecimal getMargen() {
            if (apostado.signum() == 0) {
                return null;
            }
            return getBeneficio().multiply(BigDecimal.valueOf(100)).divide(apostado, 1, RoundingMode.HALF_UP);
        }
    }
}

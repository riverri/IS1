package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Estadísticas de un conjunto de apuestas resueltas (HU-35).
 *
 * @param resueltas número de apuestas ganadas o perdidas
 * @param ganadas   número de apuestas ganadas
 * @param apostado  suma de los importes de las apuestas resueltas
 * @param beneficio ganancias menos importes apostados
 */
public record Estadisticas(int resueltas, int ganadas, BigDecimal apostado, BigDecimal beneficio) {

    public static Estadisticas de(List<Apuesta> apuestas) {
        List<Apuesta> resueltas = apuestas.stream().filter(Apuesta::isResuelta).toList();
        int ganadas = (int) resueltas.stream().filter(a -> a.getEstado() == EstadoApuesta.GANADA).count();
        BigDecimal apostado = resueltas.stream().map(Apuesta::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal beneficio = resueltas.stream().map(Apuesta::getBeneficio).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Estadisticas(resueltas.size(), ganadas, apostado, beneficio);
    }

    /** A partir de los totales de la base de datos: beneficio = pagado − apostado. */
    static Estadisticas de(long resueltas, long ganadas, BigDecimal apostado, BigDecimal pagado) {
        return new Estadisticas((int) resueltas, (int) ganadas, apostado, pagado.subtract(apostado));
    }

    static final Estadisticas VACIAS = new Estadisticas(0, 0, BigDecimal.ZERO, BigDecimal.ZERO);

    /** Porcentaje de apuestas acertadas (0 si no hay ninguna resuelta). */
    public BigDecimal getPorcentajeAciertos() {
        if (resueltas == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(ganadas * 100L).divide(BigDecimal.valueOf(resueltas), 1, RoundingMode.HALF_UP);
    }

    /** Rentabilidad: beneficio / apostado, en porcentaje (0 si no hay nada apostado). */
    public BigDecimal getRentabilidad() {
        if (apostado.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return beneficio.multiply(BigDecimal.valueOf(100)).divide(apostado, 1, RoundingMode.HALF_UP);
    }

    public boolean isTieneDatos() {
        return resueltas > 0;
    }
}

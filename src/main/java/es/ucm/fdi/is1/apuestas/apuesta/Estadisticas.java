package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Estadísticas de un conjunto de apuestas resueltas (HU-35). Se usan en Mis apuestas, en el ranking
 * (HU-36) y en el perfil público (HU-48). Las anuladas y canceladas no cuentan.
 *
 * @param resueltas número de apuestas ganadas o perdidas
 * @param ganadas   número de apuestas ganadas
 * @param apostado  suma de los importes de las apuestas resueltas
 * @param beneficio ganancias menos importes apostados
 */
public record Estadisticas(int resueltas, int ganadas, BigDecimal apostado, BigDecimal beneficio) {

    /** A partir de las apuestas de un usuario; se queda solo con las ganadas y perdidas. */
    public static Estadisticas de(List<Apuesta> apuestas) {
        // Método "fábrica" static: se llama Estadisticas.de(lista), sin necesidad de un objeto previo
        List<Apuesta> resueltas = apuestas.stream().filter(Apuesta::isResuelta).toList();
        int ganadas = (int) resueltas.stream().filter(a -> a.getEstado() == EstadoApuesta.GANADA).count();
        BigDecimal apostado = resueltas.stream().map(Apuesta::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal beneficio = resueltas.stream().map(Apuesta::getBeneficio).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Estadisticas(resueltas.size(), ganadas, apostado, beneficio);
    }

    /**
     * A partir de los totales de la base de datos ({@link ApuestaRepository#resumenResueltasPorUsuario}):
     * beneficio = pagado − apostado. Da lo mismo que sumar {@link Apuesta#getBeneficio} una a una.
     */
    static Estadisticas de(long resueltas, long ganadas, BigDecimal apostado, BigDecimal pagado) {
        return new Estadisticas((int) resueltas, (int) ganadas, apostado, pagado.subtract(apostado));
    }

    /** Para los jugadores sin ninguna apuesta resuelta. */
    static final Estadisticas VACIAS = new Estadisticas(0, 0, BigDecimal.ZERO, BigDecimal.ZERO);

    /** Porcentaje de apuestas acertadas (0 si no hay ninguna resuelta). */
    public BigDecimal getPorcentajeAciertos() {
        if (resueltas == 0) {
            return BigDecimal.ZERO;
        }
        // ganadas × 100 / resueltas, con un decimal. HALF_UP: redondeo normal (12,35 → 12,4)
        return BigDecimal.valueOf(ganadas * 100L).divide(BigDecimal.valueOf(resueltas), 1, RoundingMode.HALF_UP);
    }

    /**
     * Rentabilidad: beneficio / apostado, en porcentaje con un decimal (0 si no hay nada apostado).
     * Negativa si el balance es de pérdidas; −100 % si se ha perdido todo lo apostado.
     */
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

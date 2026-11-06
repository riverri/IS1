package es.ucm.fdi.is1.apuestas.prediccion;

/**
 * Creador de mercado automático LMSR (Logarithmic Market Scoring Rule, de R. Hanson) para mercados de sí o no
 * (HU-64, HU-65). Es quien siempre compra y vende: no hace falta que haya otro usuario al otro lado.
 *
 * <p>Con {@code qSi} y {@code qNo} las participaciones vendidas de cada lado y {@code b} la liquidez:
 * <ul>
 *   <li>Función de coste: {@code C(q) = b · ln(e^(qSi/b) + e^(qNo/b))}.</li>
 *   <li>Precio del sí: {@code e^(qSi/b) / (e^(qSi/b) + e^(qNo/b))}, entre 0 y 1; el del no es 1 − ese. Se lee como la
 *       probabilidad que el mercado da a que la respuesta sea sí.</li>
 *   <li>Comprar Δ participaciones cuesta {@code C(q + Δ) − C(q)}; venderlas devuelve {@code C(q) − C(q − Δ)}.</li>
 *   <li>Cada participación ganadora paga 1 monedita; por eso un precio de 0,63 son 0,63 monedas por participación.</li>
 *   <li>Lo más que puede perder la casa en un mercado es {@code b · ln 2} (con b = 100, unas 69 monedas).</li>
 * </ul>
 * Aquí todo es {@code double}: son exponenciales y logaritmos. Quien llama redondea el dinero siempre a favor de la
 * casa, como hace {@code CalculadoraCuotas}. Las cuentas usan la forma "estable" (restando el máximo antes de la
 * exponencial) para que no se desborden con muchas participaciones.
 */
public final class Lmsr {

    private Lmsr() {
    }

    /** {@code C(q) = b · ln(e^(qSi/b) + e^(qNo/b))}. */
    public static double coste(double qSi, double qNo, double b) {
        double a = qSi / b;
        double c = qNo / b;
        double m = Math.max(a, c);
        return b * (m + Math.log(Math.exp(a - m) + Math.exp(c - m)));
    }

    /** Precio (probabilidad) del sí: 1 / (1 + e^((qNo − qSi)/b)). Con qSi = qNo vale 0,5. */
    public static double precioSi(double qSi, double qNo, double b) {
        return 1 / (1 + Math.exp((qNo - qSi) / b));
    }

    /**
     * Participaciones de {@code lado} que se compran con {@code dinero} moneditas (ya sin comisión). Sale de despejar
     * {@code C(q + Δ) = C(q) + dinero}: para el sí, {@code qSi' = b · ln(e^((C + dinero)/b) − e^(qNo/b))}.
     */
    public static double participacionesPorDinero(boolean si, double dinero, double qSi, double qNo, double b) {
        double propio = si ? qSi : qNo;
        double otro = si ? qNo : qSi;
        double x = (coste(qSi, qNo, b) + dinero) / b;
        // b · ln(e^x − e^(otro/b)) = b · x + b · ln(1 − e^(otro/b − x)); log1p es ln(1 + y) preciso para y pequeño
        double nuevo = b * x + b * Math.log1p(-Math.exp(otro / b - x));
        return nuevo - propio;
    }

    /** Lo que devuelve vender {@code participaciones} de {@code lado}: {@code C(q) − C(q − Δ)}. */
    public static double dineroPorVenta(boolean si, double participaciones, double qSi, double qNo, double b) {
        double despuesSi = si ? qSi - participaciones : qSi;
        double despuesNo = si ? qNo : qNo - participaciones;
        return coste(qSi, qNo, b) - coste(despuesSi, despuesNo, b);
    }

    /** Lo más que puede perder la casa en un mercado de liquidez {@code b}: b · ln 2. */
    public static double perdidaMaxima(double b) {
        return b * Math.log(2);
    }
}

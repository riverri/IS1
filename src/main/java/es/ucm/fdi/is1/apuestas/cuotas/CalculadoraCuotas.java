package es.ucm.fdi.is1.apuestas.cuotas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import es.ucm.fdi.is1.apuestas.eventos.Evento;

/**
 * Algoritmo de cuotas, versión 2 (HU-02, HU-03 y fila 3 del backlog).
 *
 * <ol>
 *   <li>Nivel de cada equipo = calidad + forma reciente × {@value #VALOR_FORMA} (HU-02).</li>
 *   <li>Diferencia de nivel = (nivel local + ventaja de jugar en casa) - nivel visitante.</li>
 *   <li>Probabilidad de empate: alta si los equipos están igualados y baja si no (solo fútbol).</li>
 *   <li>El resto se reparte entre local y visitante con una curva logística según la diferencia.</li>
 *   <li>Ajuste por volumen: la probabilidad se mezcla con el reparto del dinero apostado. Cuanto más dinero
 *       hay en juego, más pesa, hasta un máximo de {@value #PESO_MAXIMO_VOLUMEN}. Así baja la cuota del
 *       resultado al que apuesta casi todo el mundo y sube la de los demás.</li>
 *   <li>Se aplica el margen de la casa y se redondea hacia abajo, así la suma de 1/cuota es siempre mayor que 1.</li>
 * </ol>
 */
@Service
public class CalculadoraCuotas {

    static final double VENTAJA_LOCAL = 0.5;
    static final double VALOR_FORMA = 0.4;
    static final double PENDIENTE = 0.45;
    static final double EMPATE_MAXIMO = 0.30;
    static final double EMPATE_MINIMO = 0.10;
    static final double EMPATE_POR_PUNTO = 0.02;
    static final double PESO_MAXIMO_VOLUMEN = 0.30;
    /** Con este importe total apostado, el volumen pesa la mitad de su máximo. */
    static final double VOLUMEN_REFERENCIA = 1000;
    static final double MARGEN_CASA = 1.07;
    static final BigDecimal CUOTA_MINIMA = new BigDecimal("1.01");
    static final BigDecimal CUOTA_MAXIMA = new BigDecimal("50.00");

    private final VolumenApostado volumen;

    public CalculadoraCuotas(VolumenApostado volumen) {
        this.volumen = volumen;
    }

    public Cuotas calcular(Evento evento) {
        return calcular(nivel(evento.getLocal().getCalidad(), evento.getLocal().getForma().getValor()),
                nivel(evento.getVisitante().getCalidad(), evento.getVisitante().getForma().getValor()),
                evento.getDeporte().isAdmiteEmpate(), volumen.importes(evento));
    }

    /** Calificación ajustada con la forma reciente (de −2 a +2). */
    static double nivel(double calidad, int forma) {
        return calidad + VALOR_FORMA * forma;
    }

    /** Cuotas solo con el nivel de los equipos, sin dinero apostado. */
    public Cuotas calcular(double nivelLocal, double nivelVisitante, boolean admiteEmpate) {
        return calcular(nivelLocal, nivelVisitante, admiteEmpate, Map.of());
    }

    public Cuotas calcular(double nivelLocal, double nivelVisitante, boolean admiteEmpate,
                           Map<Resultado, BigDecimal> apostado) {
        double diferencia = nivelLocal + VENTAJA_LOCAL - nivelVisitante;

        Map<Resultado, Double> probabilidades = new EnumMap<>(Resultado.class);
        double probabilidadEmpate = 0;
        if (admiteEmpate) {
            probabilidadEmpate = Math.max(EMPATE_MINIMO, EMPATE_MAXIMO - EMPATE_POR_PUNTO * Math.abs(diferencia));
            probabilidades.put(Resultado.EMPATE, probabilidadEmpate);
        }
        double repartoLocal = 1 / (1 + Math.exp(-PENDIENTE * diferencia));
        probabilidades.put(Resultado.LOCAL, (1 - probabilidadEmpate) * repartoLocal);
        probabilidades.put(Resultado.VISITANTE, (1 - probabilidadEmpate) * (1 - repartoLocal));

        ajustarPorVolumen(probabilidades, apostado);

        return new Cuotas(
                cuota(probabilidades.get(Resultado.LOCAL)),
                admiteEmpate ? cuota(probabilidades.get(Resultado.EMPATE)) : null,
                cuota(probabilidades.get(Resultado.VISITANTE)));
    }

    /**
     * p' = (1 - peso) × p + peso × (dinero en ese resultado / dinero total).
     * Las dos partes suman 1, así que el margen de la casa se mantiene.
     */
    private static void ajustarPorVolumen(Map<Resultado, Double> probabilidades, Map<Resultado, BigDecimal> apostado) {
        double total = probabilidades.keySet().stream()
                .mapToDouble(r -> apostado.getOrDefault(r, BigDecimal.ZERO).doubleValue())
                .sum();
        if (total <= 0) {
            return;
        }
        double peso = PESO_MAXIMO_VOLUMEN * total / (total + VOLUMEN_REFERENCIA);
        probabilidades.replaceAll((resultado, p) ->
                (1 - peso) * p + peso * apostado.getOrDefault(resultado, BigDecimal.ZERO).doubleValue() / total);
    }

    /** Cuota = 1 / (probabilidad × margen), redondeada hacia abajo a 2 decimales y acotada. */
    private static BigDecimal cuota(double probabilidad) {
        BigDecimal cuota = BigDecimal.valueOf(1 / (probabilidad * MARGEN_CASA)).setScale(2, RoundingMode.DOWN);
        return cuota.max(CUOTA_MINIMA).min(CUOTA_MAXIMA);
    }
}

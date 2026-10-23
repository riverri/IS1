package es.ucm.fdi.is1.apuestas.cuotas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;
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
 *
 * <p>Otros tipos de apuesta en fútbol (HU-52):
 * <ul>
 *   <li>Doble oportunidad: suma de las probabilidades de los dos resultados (1X = local + empate).</li>
 *   <li>Goles: cada equipo marca según una distribución de Poisson con media
 *       {@value #GOLES_MEDIOS} × e^(±{@value #GOLES_POR_PUNTO} × diferencia). Más de 2,5 = 1 − P(2 goles o menos); ambos marcan = P(local marca) × P(visitante marca).</li>
 *   <li>Se aplica el mismo margen y los mismos límites; aquí no hay ajuste por volumen.</li>
 * </ul>
 *
 * <p>Las cuotas no se guardan en el evento: se calculan cada vez que se piden. Lo usan el catálogo, el boleto y
 * {@code ApuestaService}, que copia la cuota en la selección al apostar. El dinero apostado lo da
 * {@link VolumenApostado}. Hay un ejemplo resuelto en {@code docs/wiki/Algoritmo-de-cuotas.md}.
 *
 * <p><b>Ejemplo rápido</b> (fútbol, sin apuestas todavía): local de nivel 7,0 contra visitante de nivel 6,0.
 * <ol>
 *   <li>Diferencia = 7,0 + 0,5 − 6,0 = 1,5.</li>
 *   <li>Empate = 0,30 − 0,02 × 1,5 = 0,27 (27 %).</li>
 *   <li>Curva logística: 1 / (1 + e^(−0,45 × 1,5)) ≈ 0,663. Queda un 73 % que no es empate:
 *       local = 0,73 × 0,663 ≈ 0,484 y visitante = 0,73 × 0,337 ≈ 0,246.</li>
 *   <li>Cuota = 1 / (probabilidad × 1,07): local 1,93, empate 3,46 y visitante 3,79.</li>
 * </ol>
 * Cuota y probabilidad son inversas: cuanto más probable es un resultado, menos paga.
 *
 * <p>Aquí se usa {@code double} (y no BigDecimal) porque son cálculos aproximados con exponenciales
 * ({@code Math.exp}); solo la cuota final se pasa a BigDecimal con 2 decimales.
 */
@Service
public class CalculadoraCuotas {

    /** Puntos de nivel que gana el local por jugar en casa: medio punto en la escala de calidad 0–10. */
    static final double VENTAJA_LOCAL = 0.5;
    /** Lo que vale cada punto de forma (de −2 a +2): como mucho mueve el nivel ±0,8, menos que un punto de calidad. */
    static final double VALOR_FORMA = 0.4;
    /**
     * Pendiente de la curva logística: cuanto mayor, más rápido se va la probabilidad hacia el favorito.
     * Con 0,45, un punto de diferencia reparte lo que no es empate ≈ 61 % / 39 %.
     */
    static final double PENDIENTE = 0.45;
    /** Probabilidad de empate entre equipos igualados (diferencia 0). */
    static final double EMPATE_MAXIMO = 0.30;
    /** Suelo del empate: por grande que sea la diferencia, nunca baja del 10 %. */
    static final double EMPATE_MINIMO = 0.10;
    /** Lo que baja la probabilidad de empate por cada punto de diferencia; llega al mínimo con 10 puntos. */
    static final double EMPATE_POR_PUNTO = 0.02;
    /** Peso máximo del dinero apostado frente al nivel de los equipos. No se alcanza nunca: el nivel pesa siempre más del 70 %. */
    static final double PESO_MAXIMO_VOLUMEN = 0.30;
    /** Con este importe total apostado, el volumen pesa la mitad de su máximo. */
    static final double VOLUMEN_REFERENCIA = 1000;
    /**
     * Margen de la casa: las probabilidades se inflan un 7 %, así la suma de 1/cuota ronda 1,07 y la casa
     * se queda a la larga ≈ 6,5 % de lo apostado (HU-03, HU-54).
     */
    static final double MARGEN_CASA = 1.07;
    /** Cuota mínima (CA-03.3): por mucho que se apueste a un resultado, acertarlo siempre da algo de ganancia. */
    static final BigDecimal CUOTA_MINIMA = new BigDecimal("1.01");
    /** Cuota máxima: limita lo que paga la casa por un resultado muy improbable. */
    static final BigDecimal CUOTA_MAXIMA = new BigDecimal("50.00");
    /** Goles de media de cada equipo en un partido igualado (2,6 entre los dos). */
    static final double GOLES_MEDIOS = 1.30;
    /** Cuánto cambia la media de goles por punto de diferencia: el mejor marca más (× e^(0,1 × dif)) y el rival menos. */
    static final double GOLES_POR_PUNTO = 0.10;

    private final VolumenApostado volumen;

    // Spring inyecta aquí la única clase que implementa VolumenApostado (apuesta.VolumenApuestas)
    public CalculadoraCuotas(VolumenApostado volumen) {
        this.volumen = volumen;
    }

    /**
     * Cuotas 1X2 actuales de un evento: según el nivel de sus equipos, si el deporte admite empate y el dinero
     * apostado a cada resultado. La de empate es null si el deporte no lo admite.
     */
    public Cuotas calcular(Evento evento) {
        return calcular(nivelLocal(evento), nivelVisitante(evento),
                evento.getDeporte().isAdmiteEmpate(), volumen.importes(evento));
    }

    /** Cuotas de doble oportunidad, goles y ambos marcan (HU-52). Vacío si el deporte no es fútbol. */
    public Map<Especial, BigDecimal> especiales(Evento evento) {
        if (evento.getDeporte() != Deporte.FUTBOL) {
            return Map.of();
        }
        return especiales(nivelLocal(evento), nivelVisitante(evento));
    }

    /** Cuota de una selección: un resultado (1X2) o un tipo especial. Null si no se puede apostar a eso. */
    public BigDecimal cuota(Evento evento, Resultado resultado, Especial especial) {
        if (especial != null) {
            return especiales(evento).get(especial);
        }
        return resultado == null ? null : calcular(evento).de(resultado);
    }

    /** Cuotas especiales de fútbol a partir del nivel de los dos equipos (HU-52). */
    public Map<Especial, BigDecimal> especiales(double nivelLocal, double nivelVisitante) {
        // Probabilidades 1X2 por nivel, siempre con empate (es fútbol) y sin ajuste por volumen
        Map<Resultado, Double> p = probabilidades(nivelLocal, nivelVisitante, true);
        // La misma diferencia que en el 1X2, con la ventaja del local
        double diferencia = nivelLocal + VENTAJA_LOCAL - nivelVisitante;
        // Media de goles de cada equipo: con diferencia 0 los dos marcan 1,3; el de más nivel marca más
        double golesLocal = GOLES_MEDIOS * Math.exp(GOLES_POR_PUNTO * diferencia);
        double golesVisitante = GOLES_MEDIOS * Math.exp(-GOLES_POR_PUNTO * diferencia);
        // La suma de dos Poisson independientes es otra Poisson cuya media es la suma de las medias
        double total = golesLocal + golesVisitante;
        // Poisson: P(0, 1 o 2 goles en total) = e^(−total) × (1 + total + total² / 2)
        double dosOMenos = Math.exp(-total) * (1 + total + total * total / 2);
        // P(un equipo marca) = 1 − P(0 goles) = 1 − e^(−media). Se multiplican: los goles de uno no influyen en los del otro
        double ambos = (1 - Math.exp(-golesLocal)) * (1 - Math.exp(-golesVisitante));

        Map<Especial, BigDecimal> cuotas = new EnumMap<>(Especial.class);
        // Doble oportunidad: se suman las probabilidades de los dos resultados que cubre
        cuotas.put(Especial.DOBLE_1X, cuota(p.get(Resultado.LOCAL) + p.get(Resultado.EMPATE)));
        cuotas.put(Especial.DOBLE_X2, cuota(p.get(Resultado.EMPATE) + p.get(Resultado.VISITANTE)));
        cuotas.put(Especial.DOBLE_12, cuota(p.get(Resultado.LOCAL) + p.get(Resultado.VISITANTE)));
        // Más y menos de 2,5 goles son contrarios: sus probabilidades suman 1
        cuotas.put(Especial.MAS_2_5, cuota(1 - dosOMenos));
        cuotas.put(Especial.MENOS_2_5, cuota(dosOMenos));
        cuotas.put(Especial.AMBOS_SI, cuota(ambos));
        cuotas.put(Especial.AMBOS_NO, cuota(1 - ambos));
        return cuotas;
    }

    private static double nivelLocal(Evento evento) {
        return nivel(evento.getLocal().getCalidad(), evento.getLocal().getForma().getValor());
    }

    private static double nivelVisitante(Evento evento) {
        return nivel(evento.getVisitante().getCalidad(), evento.getVisitante().getForma().getValor());
    }

    /**
     * Parte de lo apostado que se queda la casa a la larga si las probabilidades son exactas:
     * 1 − 1/{@value #MARGEN_CASA} (≈ 6,5 %) en una simple (HU-54).
     */
    public static double margenTeorico() {
        return 1 - 1 / MARGEN_CASA;
    }

    /** Calificación ajustada con la forma reciente (de −2 a +2). */
    static double nivel(double calidad, int forma) {
        // Paso 1: nivel = calidad + 0,4 × forma
        return calidad + VALOR_FORMA * forma;
    }

    /** Cuotas solo con el nivel de los equipos, sin dinero apostado. */
    public Cuotas calcular(double nivelLocal, double nivelVisitante, boolean admiteEmpate) {
        return calcular(nivelLocal, nivelVisitante, admiteEmpate, Map.of());
    }

    /**
     * Cuotas 1X2 a partir del nivel de los equipos y del dinero apostado a cada resultado (pueden faltar
     * resultados sin apuestas). Si no se admite empate, la cuota de empate es null.
     */
    public Cuotas calcular(double nivelLocal, double nivelVisitante, boolean admiteEmpate,
                           Map<Resultado, BigDecimal> apostado) {
        // Pasos 2 a 4: probabilidad de cada resultado según el nivel
        Map<Resultado, Double> probabilidades = probabilidades(nivelLocal, nivelVisitante, admiteEmpate);

        // Paso 5: se mezcla con el reparto del dinero apostado
        ajustarPorVolumen(probabilidades, apostado);

        // Paso 6: margen de la casa, redondeo hacia abajo y límites
        return new Cuotas(
                cuota(probabilidades.get(Resultado.LOCAL)),
                admiteEmpate ? cuota(probabilidades.get(Resultado.EMPATE)) : null,
                cuota(probabilidades.get(Resultado.VISITANTE)));
    }

    /** Probabilidades 1X2 por nivel, sin margen; suman 1. Sin empate, el mapa no tiene clave EMPATE. */
    private static Map<Resultado, Double> probabilidades(double nivelLocal, double nivelVisitante,
                                                         boolean admiteEmpate) {
        // Paso 2: diferencia de nivel; el local suma la ventaja de jugar en casa
        double diferencia = nivelLocal + VENTAJA_LOCAL - nivelVisitante;
        Map<Resultado, Double> probabilidades = new EnumMap<>(Resultado.class);
        double probabilidadEmpate = 0;
        if (admiteEmpate) {
            // Paso 3: 0,30 con equipos igualados; baja 0,02 por punto de diferencia (a favor de cualquiera
            // de los dos, por eso el valor absoluto) y nunca baja de 0,10
            probabilidadEmpate = Math.max(EMPATE_MINIMO, EMPATE_MAXIMO - EMPATE_POR_PUNTO * Math.abs(diferencia));
            probabilidades.put(Resultado.EMPATE, probabilidadEmpate);
        }
        // Paso 4: curva logística. Con diferencia 0 da 0,5; tiende a 1 si el local es mucho mejor y a 0 si lo es
        // el visitante, pero nunca llega a 0 ni a 1: ningún resultado es imposible
        double repartoLocal = 1 / (1 + Math.exp(-PENDIENTE * diferencia));
        // Lo que no es empate se reparte entre local y visitante, así las tres probabilidades suman 1
        probabilidades.put(Resultado.LOCAL, (1 - probabilidadEmpate) * repartoLocal);
        probabilidades.put(Resultado.VISITANTE, (1 - probabilidadEmpate) * (1 - repartoLocal));
        return probabilidades;
    }

    /**
     * p' = (1 - peso) × p + peso × (dinero en ese resultado / dinero total).
     * Las dos partes suman 1, así que el margen de la casa se mantiene.
     */
    private static void ajustarPorVolumen(Map<Resultado, Double> probabilidades, Map<Resultado, BigDecimal> apostado) {
        // Solo cuenta el dinero de los resultados posibles (en un deporte sin empate no hay clave EMPATE).
        // mapToDouble convierte cada resultado en su importe como double, y sum() los suma
        double total = probabilidades.keySet().stream()
                .mapToDouble(r -> apostado.getOrDefault(r, BigDecimal.ZERO).doubleValue())
                .sum();
        // Sin apuestas, las cuotas dependen solo del nivel
        if (total <= 0) {
            return;
        }
        // Crece con el dinero en juego: casi 0 con poco dinero, 0,15 con 1.000 moneditas y tiende a 0,30 sin
        // llegar nunca. Así unas pocas apuestas apenas mueven las cuotas
        double peso = PESO_MAXIMO_VOLUMEN * total / (total + VOLUMEN_REFERENCIA);
        // Media ponderada entre la probabilidad por nivel y la parte del dinero que hay en cada resultado.
        // replaceAll cambia el valor de cada entrada del mapa por lo que devuelve la lambda
        probabilidades.replaceAll((resultado, p) ->
                (1 - peso) * p + peso * apostado.getOrDefault(resultado, BigDecimal.ZERO).doubleValue() / total);
    }

    /** Cuota = 1 / (probabilidad × margen), redondeada hacia abajo a 2 decimales y acotada. */
    private static BigDecimal cuota(double probabilidad) {
        // Multiplicar por el margen (1,07) baja todas las cuotas; redondear hacia abajo (1,789 → 1,78)
        // las baja un poco más, nunca a favor del apostante
        BigDecimal cuota = BigDecimal.valueOf(1 / (probabilidad * MARGEN_CASA)).setScale(2, RoundingMode.DOWN);
        // Entre 1,01 (CA-03.3) y 50: max sube la cuota al mínimo si se queda corta, y min la baja al máximo si se pasa
        return cuota.max(CUOTA_MINIMA).min(CUOTA_MAXIMA);
    }
}

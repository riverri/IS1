package es.ucm.fdi.is1.apuestas.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gráfico de línea sencillo para dibujar en SVG sin JavaScript, como {@code eventos.GraficoEvolucion}: aquí se
 * calculan las coordenadas y la plantilla solo pinta. Sirve para cualquier serie de valores en orden (el precio de un
 * mercado de predicción, HU-67; el de una criptomoneda, HU-69).
 *
 * @param linea   atributo {@code d} del {@code <path>} ("M x,y L x,y …")
 * @param minimo  valor que corresponde a la parte de abajo
 * @param maximo  valor que corresponde a la parte de arriba
 * @param ultimo  último valor de la serie
 */
public record GraficoLinea(int ancho, int alto, String linea, double minimo, double maximo, double ultimo) {

    static final int ANCHO = 640;
    static final int ALTO = 180;
    static final double MARGEN = 12;

    /**
     * @param valores valores en orden, del más antiguo al más reciente
     * @param minimo  valor de abajo del todo; null para usar el menor de la serie
     * @param maximo  valor de arriba del todo; null para usar el mayor de la serie
     * @return null si hay menos de dos valores (una línea necesita dos puntos)
     */
    public static GraficoLinea de(List<Double> valores, Double minimo, Double maximo) {
        if (valores.size() < 2) {
            return null;
        }
        double abajo = minimo != null ? minimo : valores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double arriba = maximo != null ? maximo : valores.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        // Una serie plana (todos iguales) se dibuja en medio en lugar de dividir entre cero
        double rango = arriba - abajo == 0 ? 1 : arriba - abajo;
        double paso = (ANCHO - 2 * MARGEN) / (valores.size() - 1);
        List<String> puntos = new ArrayList<>();
        for (int i = 0; i < valores.size(); i++) {
            double x = MARGEN + i * paso;
            // En SVG la y crece hacia abajo: el máximo va arriba
            double y = MARGEN + (ALTO - 2 * MARGEN) * (1 - (valores.get(i) - abajo) / rango);
            puntos.add((i == 0 ? "M" : "L") + redondear(x) + "," + redondear(y));
        }
        return new GraficoLinea(ANCHO, ALTO, String.join(" ", puntos), abajo, arriba, valores.getLast());
    }

    /** Un decimal y punto como separador, sea cual sea el idioma del servidor (el SVG lo exige). */
    private static String redondear(double valor) {
        return String.format(Locale.ROOT, "%.1f", valor);
    }
}

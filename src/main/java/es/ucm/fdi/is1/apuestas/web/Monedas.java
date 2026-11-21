package es.ucm.fdi.is1.apuestas.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Cantidad de monedas para los textos que se escriben en Java (correos y avisos): "1.234,50", con punto de miles y
 * siempre dos decimales, como en las páginas. Antes cada servicio tenía su propia copia.
 */
public final class Monedas {

    private Monedas() {
    }

    public static String texto(BigDecimal cantidad) {
        // DecimalFormat no es seguro entre hilos: se crea uno en cada llamada
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-ES")));
        formato.setRoundingMode(RoundingMode.HALF_UP);
        return formato.format(cantidad);
    }

    /** Lo mismo, pero cortando los decimales que sobren en vez de redondear (lo apostado y los límites). */
    public static String truncado(BigDecimal cantidad) {
        return texto(cantidad.setScale(2, RoundingMode.DOWN));
    }
}

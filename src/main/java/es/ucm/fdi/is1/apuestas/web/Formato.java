package es.ucm.fdi.is1.apuestas.web;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Utilidades de presentación para las plantillas Thymeleaf. Es un bean de Spring llamado "formato",
 * y las vistas lo llaman así: {@code ${@formato.dia(evento.fechaHora)}}.
 * Fechas en español, cuotas con coma decimal y el escudo dibujado de los equipos sin imagen.
 */
@Component("formato")
public class Formato {

    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("EEE d MMM", ES);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm", ES);
    /** Palabras que no cuentan para las iniciales del escudo (siglas del tipo de club, artículos…). */
    private static final Set<String> PALABRAS_IGNORADAS = Set.of("fc", "cf", "cd", "ca", "rc", "rcd", "ud", "sd",
            "de", "del", "la", "el", "as", "fk", "vfb", "rb", "club", "real");

    /** "Sáb 10 oct". */
    public String dia(LocalDateTime fecha) {
        // Se quitan los puntos de las abreviaturas y se pone en mayúscula la primera letra
        String texto = DIA.format(fecha).replace(".", "");
        return texto.substring(0, 1).toUpperCase(ES) + texto.substring(1);
    }

    /** Identificador para enlaces dentro de la página: "Champions League" → "champions-league". */
    public String ancla(String texto) {
        // NFD separa cada letra de su tilde, y \p{M} quita las tildes sueltas: "Fórmula" → "Formula"
        String sinTildes = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        // Todo lo que no sea letra o número pasa a "-", y después se quitan los "-" del principio y del final
        return sinTildes.toLowerCase(ES).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    /** Cuota con coma decimal: 1.85 → "1,85". Siempre dos decimales, cortando sin redondear; "–" si no hay. */
    public String cuota(java.math.BigDecimal cuota) {
        return cuota == null ? "–" : cuota.setScale(2, java.math.RoundingMode.DOWN).toPlainString().replace('.', ',');
    }

    /**
     * Precio de una criptomoneda en euros (HU-69). Por encima de 1 €, con dos decimales y separador de miles
     * ("84.312,55"); por debajo, con 4 cifras significativas, porque muchas memecoins valen 0,00001 € o menos
     * ("0,00001234"). "–" si aún no hay precio.
     */
    public String precioCripto(java.math.BigDecimal precio) {
        if (precio == null) {
            return "–";
        }
        if (precio.compareTo(java.math.BigDecimal.ONE) >= 0) {
            return new java.text.DecimalFormat("#,##0.00", java.text.DecimalFormatSymbols.getInstance(ES))
                    .format(precio);
        }
        return precio.round(new java.math.MathContext(4)).stripTrailingZeros().toPlainString().replace('.', ',');
    }

    /** Lo mismo que {@link #precioCripto} para los extremos del gráfico, que van en {@code double}. */
    public String precioGrafico(double precio) {
        return precioCripto(java.math.BigDecimal.valueOf(precio));
    }

    /**
     * Cantidad de criptomoneda: con miles de unidades, como mucho 2 decimales ("1.250.000,5"); con menos de una,
     * 6 cifras significativas ("0,00123456").
     */
    public String cantidadCripto(java.math.BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() == 0) {
            return "0";
        }
        if (cantidad.compareTo(java.math.BigDecimal.ONE) >= 0) {
            return new java.text.DecimalFormat("#,##0.##", java.text.DecimalFormatSymbols.getInstance(ES))
                    .format(cantidad);
        }
        return cantidad.round(new java.math.MathContext(6)).stripTrailingZeros().toPlainString().replace('.', ',');
    }

    /** Variación en %, con signo y un decimal: "+12,3 %", "-4,5 %"; "–" si no hay. */
    public String variacion(java.math.BigDecimal porcentaje) {
        if (porcentaje == null) {
            return "–";
        }
        java.math.BigDecimal redondeado = porcentaje.setScale(1, java.math.RoundingMode.HALF_UP);
        return (redondeado.signum() > 0 ? "+" : "") + redondeado.toPlainString().replace('.', ',') + " %";
    }

    /**
     * Nombre corto para el campo de la alineación: "Lamine Yamal" → "Yamal", "Pedri" → "Pedri",
     * "Frenkie de Jong" → "de Jong" (se mantienen las partículas en minúscula delante del apellido).
     */
    public String apellido(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        int inicio = partes.length - 1;
        // Se retrocede mientras la palabra anterior empiece por minúscula ("de", "van"…), sin llegar a la primera
        while (inicio > 1 && Character.isLowerCase(partes[inicio - 1].charAt(0))) {
            inicio--;
        }
        return String.join(" ", Arrays.copyOfRange(partes, inicio, partes.length));
    }

    /** "21:00". */
    public String hora(LocalDateTime fecha) {
        return HORA.format(fecha);
    }

    /**
     * Iniciales para el escudo genérico, como mucho dos: "Racing de Santander" → "RS", "FC Barcelona" → "B",
     * "Real Madrid" → "M" (se saltan las palabras de PALABRAS_IGNORADAS).
     */
    public String iniciales(String nombre) {
        String[] palabras = nombre.split("[\\s/-]+");
        String significativas = Arrays.stream(palabras)
                .filter(p -> !PALABRAS_IGNORADAS.contains(p.toLowerCase(ES)))
                .map(p -> p.substring(0, 1).toUpperCase(ES))
                .collect(Collectors.joining());
        // Si todas las palabras se ignoran, la inicial de la primera
        if (significativas.isEmpty()) {
            significativas = palabras[0].substring(0, 1).toUpperCase(ES);
        }
        return significativas.length() > 2 ? significativas.substring(0, 2) : significativas;
    }

    /**
     * Color estable para el escudo de cada equipo, calculado a partir del nombre: el mismo nombre
     * da siempre el mismo tono (de 0 a 359 en la rueda de color).
     */
    public String color(String nombre) {
        // hashCode() convierte el texto en un número (siempre el mismo para el mismo texto, puede ser negativo);
        // floorMod lo deja entre 0 y 359 aunque sea negativo
        int tono = Math.floorMod(nombre.hashCode(), 360);
        return "hsl(" + tono + " 55% 38%)";
    }

    /**
     * Colores del escudo dibujado: {fondo, borde, texto}. Usa los colores del club si se conocen;
     * el texto es oscuro sobre fondos claros y blanco sobre fondos oscuros.
     */
    public String[] colores(String nombre) {
        String[] club = Colores.CLUBES.get(nombre);
        if (club == null) {
            return new String[] {color(nombre), "#ffffff", "#ffffff"};
        }
        String texto = esClaro(club[0]) ? (esClaro(club[1]) ? "#0f172a" : club[1]) : "#ffffff";
        return new String[] {club[0], club[1], texto};
    }

    /** Si un color "#rrggbb" es claro, según su luminosidad percibida (el verde pesa más que el azul). */
    private static boolean esClaro(String hex) {
        // "#a50044": cada pareja de caracteres es un número en base 16 (de 00 a ff, es decir, de 0 a 255)
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return (0.299 * r + 0.587 * g + 0.114 * b) > 170;
    }
}

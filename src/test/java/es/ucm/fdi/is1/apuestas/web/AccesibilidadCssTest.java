package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Accesibilidad en las hojas de estilo (HU-99, HU-100 y HU-101): contraste de los colores de la web en modo claro y
 * oscuro (nivel AA de WCAG 2.1), líneas del gráfico que no se distinguen solo por el color y «reducir movimiento».
 */
class AccesibilidadCssTest {

    private static final Path CSS = Path.of("src/main/resources/static/css");

    private static String leer(String archivo) throws IOException {
        return Files.readString(CSS.resolve(archivo));
    }

    /** Variables de color de los bloques {@code :root}; si una se repite, vale la última, como en el navegador. */
    private static Map<String, String> variables(String css) {
        Map<String, String> vars = new HashMap<>();
        Matcher bloque = Pattern.compile(":root\\s*\\{([^}]*)}").matcher(css);
        while (bloque.find()) {
            Matcher v = Pattern.compile("--([a-z0-9-]+)\\s*:\\s*(#[0-9a-fA-F]{3,6})").matcher(bloque.group(1));
            while (v.find()) {
                vars.put(v.group(1), v.group(2));
            }
        }
        return vars;
    }

    private static double luminancia(String hex) {
        String h = hex.substring(1);
        if (h.length() == 3) {
            h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
        }
        double[] c = new double[3];
        for (int i = 0; i < 3; i++) {
            double x = Integer.parseInt(h.substring(2 * i, 2 * i + 2), 16) / 255.0;
            c[i] = x <= 0.03928 ? x / 12.92 : Math.pow((x + 0.055) / 1.055, 2.4);
        }
        return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
    }

    /** Contraste de WCAG entre dos colores: de 1 (iguales) a 21 (negro sobre blanco). */
    static double contraste(String a, String b) {
        double la = luminancia(a), lb = luminancia(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static void comprobar(Map<String, String> vars, String modo, String texto, String fondo) {
        assertThat(contraste(vars.get(texto), vars.get(fondo)))
                .as("%s: --%s sobre --%s", modo, texto, fondo).isGreaterThanOrEqualTo(4.5);
    }

    /** CA-99.1: los textos tienen un contraste de al menos 4,5:1, con colores claros y oscuros. */
    @Test
    void losTextosTienenContrasteSuficienteEnLosDosModos() throws IOException {
        Map<String, String> claro = variables(leer("estilos.css"));
        Map<String, String> oscuro = new HashMap<>(claro);
        oscuro.putAll(variables(leer("oscuro.css")));
        for (Map<String, String> vars : java.util.List.of(claro, oscuro)) {
            String modo = vars == claro ? "claro" : "oscuro";
            for (String fondo : new String[] {"fondo", "superficie"}) {
                comprobar(vars, modo, "texto", fondo);
                comprobar(vars, modo, "texto-suave", fondo);
                comprobar(vars, modo, "primario-oscuro", fondo);
            }
            comprobar(vars, modo, "error", "error-suave");
            comprobar(vars, modo, "exito", "exito-suave");
            comprobar(vars, modo, "primario-oscuro", "primario-suave");
        }
    }

    /** CA-99.1: el texto blanco de los botones tiene contraste de 4,5:1 con todos sus fondos. */
    @Test
    void losBotonesTienenContrasteSuficiente() throws IOException {
        String css = leer("estilos.css");
        String accesibilidad = css.substring(css.indexOf("Accesibilidad (HU-97 a HU-101)"));
        Matcher regla = Pattern.compile("(?m)^\\.boton(?:-peligro)?[^{\\n]*\\{[^}]*}").matcher(accesibilidad);
        int colores = 0;
        while (regla.find()) {
            Matcher hex = Pattern.compile("#[0-9a-fA-F]{6}").matcher(regla.group());
            while (hex.find()) {
                colores++;
                assertThat(contraste("#ffffff", hex.group())).as("blanco sobre %s", hex.group())
                        .isGreaterThanOrEqualTo(4.5);
            }
        }
        assertThat(colores).isGreaterThanOrEqualTo(6);
    }

    /** CA-100.2: en el gráfico de la cuota, el empate y el visitante también se distinguen por el trazo. */
    @Test
    void lasLineasDelGraficoSeDistinguenPorElTrazo() throws IOException {
        String css = leer("estilos.css");
        assertThat(css).containsPattern("\\.evolucion-cuotas \\.linea-empate \\{[^}]*stroke-dasharray");
        assertThat(css).containsPattern("\\.evolucion-cuotas \\.linea-visitante \\{[^}]*stroke-dasharray");
    }

    /** CA-101.1: con «reducir movimiento» se quitan las animaciones y transiciones. */
    @Test
    void conReducirMovimientoNoHayAnimaciones() throws IOException {
        String css = leer("estilos.css");
        int bloque = css.indexOf("@media (prefers-reduced-motion: reduce)");
        assertThat(bloque).isPositive();
        String reglas = css.substring(bloque);
        assertThat(reglas).contains("animation-duration: .01ms !important").contains("transition-duration: .01ms !important");
    }

    /** CA-101.2: ninguna animación se repite sin fin (nada parpadea ni se mueve solo). */
    @Test
    void ningunaAnimacionSeRepiteSinFin() throws IOException {
        for (String archivo : new String[] {"estilos.css", "oscuro.css"}) {
            assertThat(leer(archivo)).as(archivo).doesNotContain("infinite");
        }
    }
}

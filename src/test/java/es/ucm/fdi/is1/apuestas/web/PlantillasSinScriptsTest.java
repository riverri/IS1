package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * La política de seguridad (CSP) solo deja ejecutar los archivos de /js (script-src 'self'): un script dentro del
 * HTML o un atributo onclick no funcionaría. Esta prueba avisa antes, al escribir la plantilla.
 * Para pedir confirmación antes de enviar un formulario se usa {@code data-confirmar="¿Seguro?"} (js/comun.js).
 */
class PlantillasSinScriptsTest {

    private static final Path PLANTILLAS = Path.of("src/main/resources/templates");
    /** onclick=, onsubmit=, th:onclick=… (cualquier atributo de evento) */
    private static final Pattern MANEJADOR = Pattern.compile("\\s(?:th:)?on[a-z]+\\s*=", Pattern.CASE_INSENSITIVE);
    /** <script> sin src: código dentro de la página */
    private static final Pattern SCRIPT_EN_LINEA = Pattern.compile("<script(?![^>]*\\bsrc=)[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_URL = Pattern.compile("=\\s*\"\\s*javascript:", Pattern.CASE_INSENSITIVE);

    @Test
    void ningunaPlantillaLlevaJavaScriptDentro() throws IOException {
        List<String> problemas = new ArrayList<>();
        try (Stream<Path> archivos = Files.walk(PLANTILLAS)) {
            for (Path archivo : archivos.filter(a -> a.toString().endsWith(".html")).toList()) {
                String html = Files.readString(archivo);
                for (Pattern patron : List.of(MANEJADOR, SCRIPT_EN_LINEA, JAVASCRIPT_URL)) {
                    Matcher m = patron.matcher(html);
                    while (m.find()) {
                        int linea = html.substring(0, m.start()).split("\n", -1).length;
                        problemas.add(PLANTILLAS.relativize(archivo) + ":" + linea + " " + m.group().trim());
                    }
                }
            }
        }
        assertThat(problemas).as("JavaScript dentro de las plantillas (la CSP no lo dejaría ejecutar)").isEmpty();
    }
}

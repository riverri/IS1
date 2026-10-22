package es.ucm.fdi.is1.apuestas.usuarios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lista de palabras que no se permiten en el nombre visible de un usuario (issue #75, CA-11.3 y CA-47.3).
 * Las palabras están en {@code nombres-prohibidos.txt}, una por línea, para poder ampliarlas sin tocar el código.
 *
 * <p>Antes de comparar, el nombre se normaliza: minúsculas, sin tildes y con las cifras que suelen sustituir
 * letras cambiadas por ellas ({@code p0ll4} → {@code polla}). Una palabra corta solo cuenta si aparece como
 * palabra completa, para no rechazar nombres como "Dickens"; una de 6 letras o más cuenta también dentro de otra
 * palabra o del nombre escrito todo junto ("cabronazo", "gili pollas").
 */
public final class NombresProhibidos {

    /** A partir de esta longitud, la palabra se busca también dentro de otras. */
    static final int LONGITUD_SUBCADENA = 6;

    private static final Set<String> PALABRAS = cargar();

    private NombresProhibidos() {
    }

    /** {@code true} si el nombre contiene alguna palabra de la lista. Un nombre nulo o vacío no contiene ninguna. */
    public static boolean contiene(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        String normalizado = normalizar(nombre);
        // Palabras sueltas: "Juan Puta" → [juan, puta]
        String[] palabras = normalizado.split("[^a-z]+");
        if (Arrays.stream(palabras).anyMatch(PALABRAS::contains)) {
            return true;
        }
        // Las largas, también dentro de una palabra o con el nombre escrito todo junto
        String junto = normalizado.replaceAll("[^a-z]", "");
        return PALABRAS.stream()
                .filter(p -> p.length() >= LONGITUD_SUBCADENA)
                .anyMatch(p -> junto.contains(p));
    }

    /** Minúsculas, sin tildes ni diéresis y con las cifras y símbolos habituales cambiados por letras. */
    static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        StringBuilder resultado = new StringBuilder(sinTildes.length());
        for (char c : sinTildes.toCharArray()) {
            resultado.append(switch (c) {
                case '0' -> 'o';
                case '1', '!' -> 'i';
                case '3' -> 'e';
                case '4', '@' -> 'a';
                case '5', '$' -> 's';
                case '7' -> 't';
                default -> c;
            });
        }
        return resultado.toString();
    }

    /** Lee la lista del classpath; se ignoran las líneas vacías y las que empiezan por #. */
    private static Set<String> cargar() {
        InputStream entrada = NombresProhibidos.class.getResourceAsStream("/nombres-prohibidos.txt");
        if (entrada == null) {
            throw new IllegalStateException("Falta nombres-prohibidos.txt en el classpath");
        }
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            return lector.lines()
                    .map(String::trim)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .map(NombresProhibidos::normalizar)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

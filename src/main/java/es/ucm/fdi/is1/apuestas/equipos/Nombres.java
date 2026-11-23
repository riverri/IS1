package es.ucm.fdi.is1.apuestas.equipos;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilidades para comparar nombres de equipos y textos sin tildes ni mayúsculas.
 * Las usan los buscadores (HU-22) y la sincronización con la API, para reconocer un equipo que allí
 * se llama de otra forma. Clase de métodos estáticos: no se instancia.
 */
public final class Nombres {

    /** Palabras que no distinguen a un club: "FC Barcelona" y "Barcelona" son el mismo. */
    private static final Set<String> GENERICAS = Set.of("fc", "cf", "cd", "ca", "rc", "rcd", "ud", "sd", "afc",
            "club", "de", "del", "la", "el", "futbol", "calcio", "ac", "as", "ssc", "fk", "sk", "sc", "1");

    /**
     * Letras (ya en minúscula) que {@link #normalizar} cambia, y su versión sin marcas, en el mismo orden. Sirven para
     * que una consulta haga en SQL lo mismo que {@code normalizar}: {@code translate(lower(nombre), CON_MARCAS,
     * SIN_MARCAS)}. Cubre Latin-1 y Latin Extended-A/B (ç, ö, ã, ł no, porque NFD no la descompone, igual que Java).
     */
    public static final String CON_MARCAS;
    public static final String SIN_MARCAS;

    static {
        StringBuilder con = new StringBuilder();
        StringBuilder sin = new StringBuilder();
        for (char c = '\u00C0'; c <= '\u024F'; c++) {
            char minuscula = Character.toLowerCase(c);
            String limpia = normalizar(String.valueOf(minuscula));
            if (minuscula == c && limpia.length() == 1 && limpia.charAt(0) != c) {
                con.append(c);
                sin.append(limpia.charAt(0));
            }
        }
        CON_MARCAS = con.toString();
        SIN_MARCAS = sin.toString();
    }

    private Nombres() {
    }

    /** "Atlético" → "atletico". Quita los espacios de los extremos; null se convierte en "". */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        // NFD separa cada letra de su tilde ("é" → "e" + "´") y \p{M} borra esas marcas
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    /** Clave para reconocer un club con nombres distintos: "Club Atlético de Madrid" → "atletico madrid". */
    public static String clave(String nombre) {
        // Se parte por todo lo que no sea letra o número y se quitan las palabras genéricas;
        // joining(" ") vuelve a unir las palabras que quedan, separadas por un espacio
        return Arrays.stream(normalizar(nombre).split("[^a-z0-9]+"))
                .filter(p -> !p.isBlank() && !GENERICAS.contains(p))
                .collect(Collectors.joining(" "));
    }
}

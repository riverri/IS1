package es.ucm.fdi.is1.apuestas.equipos;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Utilidades para comparar nombres de equipos y textos sin tildes ni mayúsculas. */
public final class Nombres {

    /** Palabras que no distinguen a un club: "FC Barcelona" y "Barcelona" son el mismo. */
    private static final Set<String> GENERICAS = Set.of("fc", "cf", "cd", "ca", "rc", "rcd", "ud", "sd", "afc",
            "club", "de", "del", "la", "el", "futbol", "calcio", "ac", "as", "ssc", "fk", "sk", "sc", "1");

    private Nombres() {
    }

    /** "Atlético" → "atletico". */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    /** Clave para reconocer un club con nombres distintos: "Club Atlético de Madrid" → "atletico madrid". */
    public static String clave(String nombre) {
        return Arrays.stream(normalizar(nombre).split("[^a-z0-9]+"))
                .filter(p -> !p.isBlank() && !GENERICAS.contains(p))
                .collect(Collectors.joining(" "));
    }
}

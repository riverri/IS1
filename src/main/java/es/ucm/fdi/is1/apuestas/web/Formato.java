package es.ucm.fdi.is1.apuestas.web;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Utilidades de presentación para las plantillas: {@code ${@formato.fecha(evento.fechaHora)}}.
 */
@Component("formato")
public class Formato {

    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("EEE d MMM", ES);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm", ES);
    private static final Set<String> PALABRAS_IGNORADAS = Set.of("fc", "cf", "cd", "ca", "rc", "rcd", "ud", "sd",
            "de", "del", "la", "el", "as", "fk", "vfb", "rb", "club", "real");

    /** "Sáb 10 oct". */
    public String dia(LocalDateTime fecha) {
        String texto = DIA.format(fecha).replace(".", "");
        return texto.substring(0, 1).toUpperCase(ES) + texto.substring(1);
    }

    /** Identificador para enlaces dentro de la página: "Champions League" → "champions-league". */
    public String ancla(String texto) {
        String sinTildes = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(ES).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    /** Cuota con coma decimal: 1.85 → "1,85". */
    public String cuota(java.math.BigDecimal cuota) {
        return cuota == null ? "–" : cuota.setScale(2, java.math.RoundingMode.DOWN).toPlainString().replace('.', ',');
    }

    /** "21:00". */
    public String hora(LocalDateTime fecha) {
        return HORA.format(fecha);
    }

    /** Iniciales para el escudo genérico: "Real Madrid" → "RM", "FC Barcelona" → "B". */
    public String iniciales(String nombre) {
        String[] palabras = nombre.split("[\\s/-]+");
        String significativas = Arrays.stream(palabras)
                .filter(p -> !PALABRAS_IGNORADAS.contains(p.toLowerCase(ES)))
                .map(p -> p.substring(0, 1).toUpperCase(ES))
                .collect(Collectors.joining());
        if (significativas.isEmpty()) {
            significativas = palabras[0].substring(0, 1).toUpperCase(ES);
        }
        return significativas.length() > 2 ? significativas.substring(0, 2) : significativas;
    }

    /** Color estable para el escudo de cada equipo, calculado a partir del nombre. */
    public String color(String nombre) {
        int tono = Math.floorMod(nombre.hashCode(), 360);
        return "hsl(" + tono + " 55% 38%)";
    }
}

package es.ucm.fdi.is1.apuestas.casino;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Apuestas posibles en la ruleta europea (HU-56), con lo que devuelven por cada moneda si se acierta (la apuesta
 * incluida): pleno 35:1 → 36, color y par/impar 1:1 → 2, docena 2:1 → 3. El 0 no es rojo ni negro, par ni impar,
 * ni de ninguna docena: con él solo gana un pleno al 0 (CA-56.2).
 */
public enum ApuestaRuleta {

    PLENO("Pleno a un número", 36),
    ROJO("Rojo", 2),
    NEGRO("Negro", 2),
    PAR("Par", 2),
    IMPAR("Impar", 2),
    DOCENA_1("1.ª docena (1–12)", 3),
    DOCENA_2("2.ª docena (13–24)", 3),
    DOCENA_3("3.ª docena (25–36)", 3);

    /** Números rojos de la ruleta europea; el resto, menos el 0, son negros. */
    static final Set<Integer> ROJOS = Set.of(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);

    private final String nombre;
    private final BigDecimal devuelve;

    ApuestaRuleta(String nombre, int devuelve) {
        this.nombre = nombre;
        this.devuelve = BigDecimal.valueOf(devuelve);
    }

    /**
     * Si la apuesta gana con el número que ha salido.
     *
     * @param elegido número apostado; solo cuenta en el pleno
     */
    public boolean gana(int salido, Integer elegido) {
        if (this == PLENO) {
            return elegido != null && elegido == salido;
        }
        // El 0 hace perder todas las demás apuestas
        if (salido == 0) {
            return false;
        }
        return switch (this) {
            case ROJO -> ROJOS.contains(salido);
            case NEGRO -> !ROJOS.contains(salido);
            case PAR -> salido % 2 == 0;
            case IMPAR -> salido % 2 == 1;
            case DOCENA_1 -> salido <= 12;
            case DOCENA_2 -> salido >= 13 && salido <= 24;
            // DOCENA_3 (PLENO ya está resuelto arriba)
            default -> salido >= 25;
        };
    }

    /** "rojo", "negro" o "verde" (el 0). */
    public static String color(int numero) {
        if (numero == 0) {
            return "verde";
        }
        return ROJOS.contains(numero) ? "rojo" : "negro";
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getDevuelve() {
        return devuelve;
    }
}

package es.ucm.fdi.is1.apuestas.equipos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Alineación probable en 4-3-3: por cada posición salen los de mejor nota; a igual nota, el dorsal
 * más bajo (los titulares suelen llevar del 1 al 11) y después el nombre.
 *
 * <p>Objeto de valor de la ficha del equipo (HU-49). Lo crea {@link PlantillaService#alineacion(Equipo)}
 * a partir de las notas que pone el creador a cada {@link Jugador}.
 *
 * @param sistema   siempre "4-3-3"
 * @param lineas    de arriba abajo: delanteros, centrocampistas, defensas y portero
 * @param notaMedia media de las notas de los once titulares, con un decimal
 */
public record Alineacion(String sistema, List<Linea> lineas, double notaMedia) {

    /** Jugadores por línea en el 4-3-3. */
    static final Map<Posicion, Integer> HUECOS = Map.of(
            Posicion.DELANTERO, 3, Posicion.CENTROCAMPISTA, 3, Posicion.DEFENSA, 4, Posicion.PORTERO, 1);

    /** Una fila del campo: los titulares de una posición, ordenados por dorsal. */
    public record Linea(Posicion posicion, List<Jugador> jugadores) {
    }

    /** Criterio para elegir titulares: nota de mayor a menor, después dorsal (sin dorsal al final) y nombre. */
    static final Comparator<Jugador> MEJOR_PRIMERO = Comparator.comparingDouble(Jugador::getNota).reversed()
            .thenComparing(Jugador::getDorsal, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Jugador::getNombre);

    /**
     * Elige los titulares de la plantilla.
     *
     * @return null si no hay jugadores suficientes en alguna posición
     */
    public static Alineacion de(List<Jugador> plantilla) {
        List<Linea> lineas = new ArrayList<>();
        // Se recorre de la delantera a la portería, que es como se pinta el campo de arriba abajo
        for (Posicion posicion : List.of(Posicion.DELANTERO, Posicion.CENTROCAMPISTA, Posicion.DEFENSA,
                Posicion.PORTERO)) {
            int cuantos = HUECOS.get(posicion);
            // Los "cuantos" mejores de esa posición
            List<Jugador> elegidos = plantilla.stream()
                    .filter(j -> j.getPosicion() == posicion)
                    .sorted(MEJOR_PRIMERO)
                    .limit(cuantos)
                    .toList();
            if (elegidos.size() < cuantos) {
                return null;
            }
            // Dentro de la línea, por dorsal, para que no cambien de sitio al cambiar una nota
            lineas.add(new Linea(posicion, elegidos.stream()
                    .sorted(Comparator.comparing(Jugador::getDorsal, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList()));
        }
        // Media de los once, redondeada a un decimal. average() devuelve un OptionalDouble (vacío si no hay
        // jugadores), por eso el orElse(0)
        double media = lineas.stream().flatMap(l -> l.jugadores().stream()).mapToDouble(Jugador::getNota)
                .average().orElse(0);
        return new Alineacion("4-3-3", lineas, Math.round(media * 10) / 10.0);
    }
}

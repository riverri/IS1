package es.ucm.fdi.is1.apuestas.equipos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Alineación probable en 4-3-3: por cada posición salen los de mejor nota; a igual nota, el dorsal
 * más bajo (los titulares suelen llevar del 1 al 11) y después el nombre.
 *
 * @param lineas de arriba abajo: delanteros, centrocampistas, defensas y portero
 */
public record Alineacion(String sistema, List<Linea> lineas, double notaMedia) {

    /** Jugadores por línea en el 4-3-3. */
    static final Map<Posicion, Integer> HUECOS = Map.of(
            Posicion.DELANTERO, 3, Posicion.CENTROCAMPISTA, 3, Posicion.DEFENSA, 4, Posicion.PORTERO, 1);

    public record Linea(Posicion posicion, List<Jugador> jugadores) {
    }

    static final Comparator<Jugador> MEJOR_PRIMERO = Comparator.comparingDouble(Jugador::getNota).reversed()
            .thenComparing(Jugador::getDorsal, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Jugador::getNombre);

    /** Null si no hay jugadores suficientes en alguna posición. */
    public static Alineacion de(List<Jugador> plantilla) {
        List<Linea> lineas = new ArrayList<>();
        for (Posicion posicion : List.of(Posicion.DELANTERO, Posicion.CENTROCAMPISTA, Posicion.DEFENSA,
                Posicion.PORTERO)) {
            int cuantos = HUECOS.get(posicion);
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
        double media = lineas.stream().flatMap(l -> l.jugadores().stream()).mapToDouble(Jugador::getNota)
                .average().orElse(0);
        return new Alineacion("4-3-3", lineas, Math.round(media * 10) / 10.0);
    }
}

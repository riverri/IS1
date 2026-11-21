package es.ucm.fdi.is1.apuestas.casino;

import java.math.BigDecimal;

/**
 * A qué se puede apostar en el baccarat (HU-74) y lo que devuelve cada monedita si se acierta, con la apuesta incluida:
 * <ul>
 *     <li>Jugador: paga 1 a 1 → devuelve 2.</li>
 *     <li>Banca: paga 1 a 1 menos un 5 % de comisión → devuelve 1,95. La Banca gana algo más a menudo que el Jugador
 *     (por la tabla de la tercera carta) y la comisión lo compensa.</li>
 *     <li>Empate: paga 8 a 1 → devuelve 9.</li>
 * </ul>
 * Si hay empate, a quien apostó a Jugador o a Banca se le devuelve lo apostado (CA-74.3).
 *
 * <p>La ventaja de la casa de cada apuesta, con 8 barajas, es la de las tablas habituales del juego.
 */
public enum ApuestaBaccarat {

    JUGADOR("Jugador", "2", "1,24"),
    BANCA("Banca", "1.95", "1,06"),
    EMPATE("Empate", "9", "14,36");

    private final String nombre;
    private final BigDecimal devuelve;
    private final String ventajaCasa;

    ApuestaBaccarat(String nombre, String devuelve, String ventajaCasa) {
        this.nombre = nombre;
        this.devuelve = new BigDecimal(devuelve);
        this.ventajaCasa = ventajaCasa;
    }

    /** Lo que se cobra por cada monedita apostada si la mano la gana {@code ganador}: 0 si se pierde. */
    public BigDecimal devuelveSiGana(ApuestaBaccarat ganador) {
        if (ganador == this) {
            return devuelve;
        }
        // Con empate, Jugador y Banca no pierden: se devuelve la apuesta
        return ganador == EMPATE ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getDevuelve() {
        return devuelve;
    }

    /** Ventaja de la casa en %, ya con coma decimal para pintarla: "1,06". */
    public String getVentajaCasa() {
        return ventajaCasa;
    }
}

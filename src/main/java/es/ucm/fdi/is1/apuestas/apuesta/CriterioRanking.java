package es.ucm.fdi.is1.apuestas.apuesta;

/** Criterios de ordenación del ranking (HU-36); el nombre es el texto que se muestra en la página. */
public enum CriterioRanking {

    /** Saldo actual de cada jugador. */
    SALDO("Saldo"),
    /** Beneficio neto de las apuestas resueltas: pagado − apostado. */
    GANANCIAS("Ganancias"),
    /** Porcentaje de apuestas ganadas; primero los que tienen un mínimo de apuestas resueltas. */
    ACIERTOS("% de aciertos");

    // Un enum puede tener atributos y constructor: cada valor de arriba, como SALDO("Saldo"),
    // llama a este constructor con su texto. El constructor de un enum no se puede llamar desde fuera
    private final String nombre;

    CriterioRanking(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}

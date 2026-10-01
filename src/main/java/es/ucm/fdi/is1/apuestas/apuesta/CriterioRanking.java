package es.ucm.fdi.is1.apuestas.apuesta;

/** Criterios de ordenación del ranking (HU-36). */
public enum CriterioRanking {

    SALDO("Saldo"),
    GANANCIAS("Ganancias"),
    ACIERTOS("% de aciertos");

    private final String nombre;

    CriterioRanking(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}

package es.ucm.fdi.is1.apuestas.cuotas;

/**
 * Resultado de un evento sobre el que se apuesta (G/E/P desde el punto de vista del local).
 * Es el pronóstico de una selección 1X2 y el resultado que se guarda al finalizar un evento (HU-04, HU-20, HU-23).
 * {@code EMPATE} solo es posible en fútbol, el único deporte que admite empate.
 */
public enum Resultado {

    LOCAL("1", "Gana el local"),
    EMPATE("X", "Empate"),
    VISITANTE("2", "Gana el visitante");

    private final String simbolo;
    private final String descripcion;

    Resultado(String simbolo, String descripcion) {
        this.simbolo = simbolo;
        this.descripcion = descripcion;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

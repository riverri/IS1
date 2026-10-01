package es.ucm.fdi.is1.apuestas.cuotas;

/** Resultado de un evento sobre el que se apuesta (G/E/P desde el punto de vista del local). */
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

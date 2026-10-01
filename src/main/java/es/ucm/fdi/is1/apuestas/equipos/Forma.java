package es.ucm.fdi.is1.apuestas.equipos;

/**
 * Factor manual de forma reciente de un equipo (HU-02): rachas, moral, lesiones…
 * Lo fija el creador de apuestas y se suma a la calificación al calcular las cuotas.
 */
public enum Forma {

    MUY_MALA(-2, "Muy mala racha", "▼▼"),
    MALA(-1, "Mala racha", "▼"),
    NORMAL(0, "Normal", "–"),
    BUENA(1, "Buena racha", "▲"),
    MUY_BUENA(2, "Muy buena racha", "▲▲");

    private final int valor;
    private final String descripcion;
    private final String simbolo;

    Forma(int valor, String descripcion, String simbolo) {
        this.valor = valor;
        this.descripcion = descripcion;
        this.simbolo = simbolo;
    }

    /** De −2 (muy mala racha) a +2 (muy buena racha). */
    public int getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getSimbolo() {
        return simbolo;
    }
}

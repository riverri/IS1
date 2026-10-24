package es.ucm.fdi.is1.apuestas.prediccion;

/** Las dos respuestas de un mercado de predicción (HU-64). */
public enum Lado {

    SI("Sí"),
    NO("No");

    private final String nombre;

    Lado(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}

package es.ucm.fdi.is1.apuestas.casino;

/** Juegos del casino (HU-56, HU-57, HU-74). Para añadir otro, se añade aquí y se escriben sus reglas aparte. */
public enum TipoJuego {

    RULETA("Ruleta europea", "🎡"),
    BLACKJACK("Blackjack", "🃏"),
    BACCARAT("Baccarat", "🎴");

    private final String nombre;
    private final String icono;

    TipoJuego(String nombre, String icono) {
        this.nombre = nombre;
        this.icono = icono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIcono() {
        return icono;
    }
}

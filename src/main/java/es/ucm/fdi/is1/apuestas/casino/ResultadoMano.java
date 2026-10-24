package es.ucm.fdi.is1.apuestas.casino;

/** Cómo acaba una mano de blackjack para el jugador (HU-57). */
public enum ResultadoMano {
    /** As y figura o diez con las dos primeras cartas, sin que la banca lo tenga también. */
    BLACKJACK("¡Blackjack!"),
    GANA("Ganas la mano"),
    /** Mismo valor que la banca: se devuelve lo apostado. */
    EMPATE("Empate: recuperas lo apostado"),
    PIERDE("Pierdes la mano");

    private final String descripcion;

    ResultadoMano(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

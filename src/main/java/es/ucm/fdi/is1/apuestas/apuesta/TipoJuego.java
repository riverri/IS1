package es.ucm.fdi.is1.apuestas.apuesta;

/**
 * Tipos de juego para filtrar el ranking (HU-84). El nombre es el texto de la pestaña. El casino no está porque no
 * forma parte de la aplicación en esta versión.
 */
public enum TipoJuego {

    /** Apuestas a eventos y a largo plazo, simples y combinadas. */
    DEPORTES("Deportes", true),
    /** Mercados de predicción ya resueltos. */
    PREDICCIONES("Predicciones", true),
    /** Criptomonedas de referencia (bitcoin, ether...). */
    CRIPTO("Cripto", false),
    MEMECOINS("Memecoins", false),
    /** Boletos de quinielas ya repartidas. */
    QUINIELA("Quiniela", true);

    private final String nombre;
    private final boolean conAciertos;

    TipoJuego(String nombre, boolean conAciertos) {
        this.nombre = nombre;
        this.conAciertos = conAciertos;
    }

    public String getNombre() {
        return nombre;
    }

    /** En cripto y memecoins no se acierta ni se falla: solo se gana o se pierde (CA-84.2). */
    public boolean isConAciertos() {
        return conAciertos;
    }

    /** Si se puede ordenar así: nunca por saldo, que es común a todos los juegos (CA-84.2). */
    public boolean admite(CriterioRanking criterio) {
        return criterio == CriterioRanking.GANANCIAS || (criterio == CriterioRanking.ACIERTOS && conAciertos);
    }
}

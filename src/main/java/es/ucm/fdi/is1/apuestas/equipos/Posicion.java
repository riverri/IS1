package es.ucm.fdi.is1.apuestas.equipos;

import java.util.Locale;

/** Demarcación de un jugador de fútbol, de la portería a la delantera. */
public enum Posicion {

    PORTERO("Portero", "Porteros", "POR"),
    DEFENSA("Defensa", "Defensas", "DEF"),
    CENTROCAMPISTA("Centrocampista", "Centrocampistas", "MED"),
    DELANTERO("Delantero", "Delanteros", "DEL");

    private final String nombre;
    private final String plural;
    private final String abreviatura;

    Posicion(String nombre, String plural, String abreviatura) {
        this.nombre = nombre;
        this.plural = plural;
        this.abreviatura = abreviatura;
    }

    /**
     * Traduce la posición de football-data.org: "Goalkeeper", "Defence", "Midfield", "Offence" o una
     * más detallada ("Centre-Back", "Left Winger", "Centre-Forward"…). Null si no se reconoce.
     */
    public static Posicion desdeApi(String posicion) {
        if (posicion == null) {
            return null;
        }
        String texto = posicion.toLowerCase(Locale.ROOT);
        if (texto.contains("goalkeeper")) {
            return PORTERO;
        }
        // Antes que "defen": "Defensive Midfield" es un centrocampista
        if (texto.contains("midfield")) {
            return CENTROCAMPISTA;
        }
        if (texto.contains("back") || texto.contains("defen")) {
            return DEFENSA;
        }
        if (texto.contains("offence") || texto.contains("forward") || texto.contains("winger")
                || texto.contains("striker") || texto.contains("attack")) {
            return DELANTERO;
        }
        return null;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPlural() {
        return plural;
    }

    public String getAbreviatura() {
        return abreviatura;
    }
}

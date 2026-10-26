package es.ucm.fdi.is1.apuestas.equipos;

/**
 * Deportes de la web, con el nombre y el icono que se muestran y dos propiedades que cambian las reglas:
 * si hay empate (mercado 1X2 o solo 1/2) y si compiten deportistas individuales en lugar de equipos.
 * El orden de los valores es el de las pestañas del catálogo y del listado de equipos.
 * (Por eso nunca se guarda en la base de datos la posición del valor, sino su nombre: ver {@code EnumType.STRING}.)
 */
public enum Deporte {

    /** El único con empate; también el único con alineación probable (HU-49) y apuestas de goles (HU-52). */
    FUTBOL("Fútbol", "⚽", true, false),
    BALONCESTO("Baloncesto", "🏀", false, false),
    TENIS("Tenis", "🎾", false, true),
    /** Fórmula 1: cada evento enfrenta a dos pilotos. */
    AUTOMOVILISMO("Fórmula 1", "🏎️", false, true),
    /** MotoGP: cada evento enfrenta a dos pilotos. */
    MOTOCICLISMO("MotoGP", "🏍️", false, true);

    private final String nombre;
    private final String icono;
    private final boolean admiteEmpate;
    private final boolean individual;

    Deporte(String nombre, String icono, boolean admiteEmpate, boolean individual) {
        this.nombre = nombre;
        this.icono = icono;
        this.admiteEmpate = admiteEmpate;
        this.individual = individual;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIcono() {
        return icono;
    }

    /** Solo el fútbol admite empate; en baloncesto, tenis o motor solo se apuesta a 1 o 2. */
    public boolean isAdmiteEmpate() {
        return admiteEmpate;
    }

    /** Deportes de deportistas individuales (tenis, motor): se enfrentan dos competidores. */
    public boolean isIndividual() {
        return individual;
    }
}

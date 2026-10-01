package es.ucm.fdi.is1.apuestas.equipos;

public enum Deporte {

    FUTBOL("Fútbol", "⚽", true, false),
    BALONCESTO("Baloncesto", "🏀", false, false),
    TENIS("Tenis", "🎾", false, true),
    AUTOMOVILISMO("Fórmula 1", "🏎️", false, true),
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

    /** En baloncesto o tenis no hay empate: solo se apuesta a 1 o 2. */
    public boolean isAdmiteEmpate() {
        return admiteEmpate;
    }

    /** Deportes de deportistas individuales (tenis, motor): se enfrentan dos competidores. */
    public boolean isIndividual() {
        return individual;
    }
}

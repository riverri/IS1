package es.ucm.fdi.is1.apuestas.equipos;

public enum Deporte {

    FUTBOL("Fútbol", true),
    BALONCESTO("Baloncesto", false),
    TENIS("Tenis", false),
    AUTOMOVILISMO("Automovilismo", false),
    MOTOCICLISMO("Motociclismo", false);

    private final String nombre;
    private final boolean admiteEmpate;

    Deporte(String nombre, boolean admiteEmpate) {
        this.nombre = nombre;
        this.admiteEmpate = admiteEmpate;
    }

    public String getNombre() {
        return nombre;
    }

    /** En baloncesto o tenis no hay empate: solo se apuesta a 1 o 2. */
    public boolean isAdmiteEmpate() {
        return admiteEmpate;
    }
}

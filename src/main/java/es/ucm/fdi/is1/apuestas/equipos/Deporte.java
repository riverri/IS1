package es.ucm.fdi.is1.apuestas.equipos;

public enum Deporte {

    FUTBOL("Fútbol"),
    BALONCESTO("Baloncesto"),
    TENIS("Tenis"),
    AUTOMOVILISMO("Automovilismo"),
    MOTOCICLISMO("Motociclismo");

    private final String nombre;

    Deporte(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}

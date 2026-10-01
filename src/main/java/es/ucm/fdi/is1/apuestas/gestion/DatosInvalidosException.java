package es.ucm.fdi.is1.apuestas.gestion;

/** Regla de negocio incumplida en un formulario; indica el campo afectado. */
public class DatosInvalidosException extends RuntimeException {

    private final String campo;

    public DatosInvalidosException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}

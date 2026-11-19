package es.ucm.fdi.is1.apuestas.usuarios;

/**
 * La contraseña actual no es la correcta: al cambiarla (HU-47) o al confirmar la baja de la cuenta (HU-18).
 * Los controladores la recogen y muestran un mensaje de error.
 */
public class PasswordIncorrectaException extends RuntimeException {

    /** Se ha frenado por demasiados fallos seguidos, sin llegar a comprobar la contraseña. */
    private final boolean demasiadosIntentos;

    public PasswordIncorrectaException() {
        super("La contraseña actual no es correcta");
        this.demasiadosIntentos = false;
    }

    /** Tras demasiados fallos seguidos (ver {@link IntentosLogin}) no se comprueba durante un rato. */
    static PasswordIncorrectaException demasiadosIntentos() {
        return new PasswordIncorrectaException("Demasiados intentos con una contraseña incorrecta: espera 15 minutos "
                + "y vuelve a probar.");
    }

    private PasswordIncorrectaException(String mensaje) {
        super(mensaje);
        this.demasiadosIntentos = true;
    }

    public boolean isDemasiadosIntentos() {
        return demasiadosIntentos;
    }
}

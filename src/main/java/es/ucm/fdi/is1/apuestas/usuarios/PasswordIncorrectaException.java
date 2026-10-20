package es.ucm.fdi.is1.apuestas.usuarios;

/**
 * La contraseña actual no es la correcta: al cambiarla (HU-47) o al confirmar la baja de la cuenta (HU-18).
 * Los controladores la recogen y muestran un mensaje de error.
 */
public class PasswordIncorrectaException extends RuntimeException {

    public PasswordIncorrectaException() {
        super("La contraseña actual no es correcta");
    }
}

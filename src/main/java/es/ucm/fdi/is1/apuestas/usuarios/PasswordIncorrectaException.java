package es.ucm.fdi.is1.apuestas.usuarios;

/** La contraseña actual no es la correcta (HU-47). */
public class PasswordIncorrectaException extends RuntimeException {

    public PasswordIncorrectaException() {
        super("La contraseña actual no es correcta");
    }
}

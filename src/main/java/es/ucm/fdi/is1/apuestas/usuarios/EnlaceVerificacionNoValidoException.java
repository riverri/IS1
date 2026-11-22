package es.ucm.fdi.is1.apuestas.usuarios;

/** Indicates that an email verification link has expired, already been used, or is malformed. */
public class EnlaceVerificacionNoValidoException extends RuntimeException {

    public EnlaceVerificacionNoValidoException() {
        super("El enlace no es válido o ha caducado");
    }
}

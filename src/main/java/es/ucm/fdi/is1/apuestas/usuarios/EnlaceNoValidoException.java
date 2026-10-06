package es.ucm.fdi.is1.apuestas.usuarios;

/** El enlace para recuperar la contraseña ha caducado, ya se ha usado o no existe (HU-17). */
public class EnlaceNoValidoException extends RuntimeException {

    public EnlaceNoValidoException() {
        super("El enlace ha caducado o ya se ha usado. Pide uno nuevo.");
    }
}

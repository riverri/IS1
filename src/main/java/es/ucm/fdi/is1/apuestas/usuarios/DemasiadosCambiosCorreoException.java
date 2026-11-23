package es.ucm.fdi.is1.apuestas.usuarios;

/** Se han pedido demasiados cambios de correo seguidos desde la cuenta o hacia la misma dirección. */
public class DemasiadosCambiosCorreoException extends RuntimeException {

    public DemasiadosCambiosCorreoException() {
        super("Has pedido varios cambios de correo seguidos. Espera media hora antes de pedir otro.");
    }
}

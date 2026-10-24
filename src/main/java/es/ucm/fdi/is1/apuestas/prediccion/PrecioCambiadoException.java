package es.ucm.fdi.is1.apuestas.prediccion;

/**
 * El precio ha cambiado desde que el usuario lo vio (otro ha comprado o vendido entretanto). No se opera y se le
 * enseña el nuevo para que decida, como con las cuotas de las combinadas (HU-29, CA-64.3).
 */
public class PrecioCambiadoException extends RuntimeException {

    public PrecioCambiadoException() {
        super("El precio ha cambiado mientras decidías. Revisa el nuevo y confirma otra vez.");
    }
}

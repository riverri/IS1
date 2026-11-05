package es.ucm.fdi.is1.apuestas.cripto;

/**
 * El precio ha cambiado desde que el usuario lo vio (ha llegado uno nuevo de la API mientras decidía). No se opera y
 * se le enseña el nuevo, como con las cuotas (HU-29) y los mercados de predicción (CA-64.3).
 */
public class PrecioCambiadoException extends RuntimeException {

    public PrecioCambiadoException() {
        super("El precio ha cambiado mientras decidías. Revisa el nuevo y confirma otra vez.");
    }
}

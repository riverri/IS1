package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

/**
 * Al cambiar el importe de una apuesta (HU-27), la cuota total actual ya no es la que vio el usuario.
 * Igual que en HU-29, no se cambia nada: el controlador le enseña la cuota nueva y él decide si la acepta.
 */
public class CuotaApuestaCambiadaException extends RuntimeException {

    /** Cuota total que tendría la apuesta con las cuotas de ahora. */
    private final BigDecimal cuotaNueva;

    public CuotaApuestaCambiadaException(BigDecimal cuotaNueva) {
        super("La cuota de la apuesta ha cambiado");
        this.cuotaNueva = cuotaNueva;
    }

    public BigDecimal getCuotaNueva() {
        return cuotaNueva;
    }
}

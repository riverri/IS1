package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

/** Lo que vale cobrar una apuesta ha cambiado desde que se enseñó (HU-75, CA-75.4). */
public class CashOutCambiadoException extends RuntimeException {

    private final BigDecimal valorNuevo;

    public CashOutCambiadoException(BigDecimal valorNuevo) {
        super("Las cuotas han cambiado: ahora puedes cobrar " + valorNuevo.toPlainString().replace('.', ',') + " monedas");
        this.valorNuevo = valorNuevo;
    }

    public BigDecimal getValorNuevo() {
        return valorNuevo;
    }
}

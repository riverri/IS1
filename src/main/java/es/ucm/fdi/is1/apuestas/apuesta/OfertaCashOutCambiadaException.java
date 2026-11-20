package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

/** La oferta cambió desde que se mostró; el usuario debe volver a confirmarla. */
public class OfertaCashOutCambiadaException extends RuntimeException {
    private final BigDecimal ofertaNueva;

    public OfertaCashOutCambiadaException(BigDecimal ofertaNueva) {
        super("La oferta de cash-out ha cambiado. Revísala y confirma de nuevo.");
        this.ofertaNueva = ofertaNueva;
    }

    public BigDecimal getOfertaNueva() { return ofertaNueva; }
}

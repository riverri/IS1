package es.ucm.fdi.is1.apuestas.casino;

import java.math.BigDecimal;

/**
 * Lo que paga un blackjack (as y figura o diez con las dos primeras cartas), a elegir por el creador (HU-59).
 * 3:2 es lo clásico; 6:5 sube bastante la ventaja de la casa.
 */
public enum PagoBlackjack {

    TRES_A_DOS("3:2", new BigDecimal("1.5")),
    SEIS_A_CINCO("6:5", new BigDecimal("1.2"));

    private final String texto;
    /** Ganancia por cada moneda apostada, sin contar la apuesta: 3:2 = 1,5. */
    private final BigDecimal ganancia;

    PagoBlackjack(String texto, BigDecimal ganancia) {
        this.texto = texto;
        this.ganancia = ganancia;
    }

    public String getTexto() {
        return texto;
    }

    public BigDecimal getGanancia() {
        return ganancia;
    }
}

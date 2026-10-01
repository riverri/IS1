package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal saldo, BigDecimal importe) {
        super("Saldo insuficiente: tienes " + saldo.toPlainString() + " y la apuesta es de " + importe.toPlainString());
    }
}

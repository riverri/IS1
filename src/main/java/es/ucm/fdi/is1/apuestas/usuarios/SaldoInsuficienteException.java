package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;

/**
 * El saldo no llega para el importe de una apuesta. La lanza {@link Usuario#cargar} y el mensaje
 * se muestra al usuario.
 */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal saldo, BigDecimal importe) {
        super("Saldo insuficiente: tienes " + saldo.toPlainString() + " y la apuesta es de " + importe.toPlainString());
    }
}

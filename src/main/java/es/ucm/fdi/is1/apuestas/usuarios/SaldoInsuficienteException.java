package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;

/**
 * El saldo no llega para el importe de una apuesta. La lanza {@link Usuario#cargar} y el mensaje
 * se muestra al usuario.
 *
 * <p>Es una excepción propia: hereda de {@code RuntimeException}, así que no hace falta declararla con
 * {@code throws}. Tener un nombre concreto permite que el controlador la capture ({@code catch}) y enseñe el
 * mensaje, distinguiéndola de otros errores. Al lanzarla dentro de un método {@code @Transactional}, se deshace
 * todo lo que ese método hubiera cambiado.
 */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal saldo, BigDecimal importe) {
        // super(...) pasa el mensaje a RuntimeException; luego se lee con getMessage()
        super("Saldo insuficiente: tienes " + saldo.toPlainString() + " y la apuesta es de " + importe.toPlainString());
    }
}

package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

/**
 * Una fila del ranking: posición, nombre visible, cifras y si es el usuario que lo consulta.
 *
 * @param posicion empieza en 1
 * @param soyYo    true en la fila del usuario con sesión, para destacarla (CA-36.4)
 */
public record PuestoRanking(int posicion, Long usuarioId, String nombre, BigDecimal saldo,
                            Estadisticas estadisticas, boolean soyYo) {
}

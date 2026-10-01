package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

/** Una fila del ranking: posición, nombre visible, cifras y si es el usuario que lo consulta. */
public record PuestoRanking(int posicion, String nombre, BigDecimal saldo, Estadisticas estadisticas,
                            boolean soyYo) {
}

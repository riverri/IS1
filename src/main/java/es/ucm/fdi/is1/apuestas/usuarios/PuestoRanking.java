package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;

/** Una fila del ranking: posición, nombre visible, saldo y si es el usuario que lo consulta. */
public record PuestoRanking(int posicion, String nombre, BigDecimal saldo, boolean soyYo) {
}

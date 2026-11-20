package es.ucm.fdi.is1.apuestas.ligas;

import java.math.BigDecimal;

/** Fila de la clasificación de las ligas del usuario, ordenadas por el saldo conjunto de sus miembros activos. */
public record PuestoClasificacionLiga(int posicion, Long ligaId, String nombre, int miembros,
                                      BigDecimal saldoTotal) {
}

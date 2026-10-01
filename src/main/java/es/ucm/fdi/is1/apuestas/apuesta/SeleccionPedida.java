package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

/**
 * Selección que pide el usuario al confirmar el boleto.
 *
 * @param cuotaVista cuota que vio el usuario; null si no hay que comprobarla
 */
public record SeleccionPedida(Long eventoId, Resultado resultado, BigDecimal cuotaVista) {
}

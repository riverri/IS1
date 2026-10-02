package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

/**
 * Selección que pide el usuario al confirmar el boleto: un resultado (1X2) o un tipo especial (HU-52).
 *
 * @param cuotaVista cuota que vio el usuario; null si no hay que comprobarla
 */
public record SeleccionPedida(Long eventoId, Resultado resultado, Especial especial, BigDecimal cuotaVista) {

    public SeleccionPedida(Long eventoId, Resultado resultado, BigDecimal cuotaVista) {
        this(eventoId, resultado, null, cuotaVista);
    }
}

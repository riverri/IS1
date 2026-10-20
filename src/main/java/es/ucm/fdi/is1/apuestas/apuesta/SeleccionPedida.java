package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

/**
 * Selección que pide el usuario al confirmar el boleto: un resultado (1X2) o un tipo especial (HU-52).
 * Es lo que recibe {@link ApuestaService#apostar(String, java.util.List, BigDecimal)}; la cuota real
 * se calcula siempre en el servidor.
 *
 * @param resultado  pronóstico 1X2; null si se pide un tipo especial
 * @param especial   tipo especial; null si se pide un resultado
 * @param cuotaVista cuota que vio el usuario; null si no hay que comprobarla
 */
public record SeleccionPedida(Long eventoId, Resultado resultado, Especial especial, BigDecimal cuotaVista) {

    /** Selección 1X2. */
    public SeleccionPedida(Long eventoId, Resultado resultado, BigDecimal cuotaVista) {
        this(eventoId, resultado, null, cuotaVista);
    }
}

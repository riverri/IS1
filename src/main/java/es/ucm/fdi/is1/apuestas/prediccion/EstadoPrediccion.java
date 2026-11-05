package es.ucm.fdi.is1.apuestas.prediccion;

/**
 * Ciclo de vida de un mercado de predicción (HU-63, HU-66):
 *
 * <pre>
 * ABIERTO ──cerrar──▶ CERRADO
 * ABIERTO / CERRADO ──resolver──▶ RESUELTO (se puede volver a resolver para corregir)
 * ABIERTO / CERRADO ──anular──▶ ANULADO (definitivo)
 * </pre>
 * Un mercado abierto deja de admitir compras y ventas al llegar su fecha de cierre, aunque nadie lo cierre a mano.
 */
public enum EstadoPrediccion {
    ABIERTO,
    CERRADO,
    RESUELTO,
    ANULADO
}

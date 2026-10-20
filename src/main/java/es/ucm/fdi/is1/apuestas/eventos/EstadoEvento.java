package es.ucm.fdi.is1.apuestas.eventos;

/**
 * Estado de un {@link Evento} en su ciclo de vida (HU-04, HU-05). Las transiciones las controla el propio
 * evento:
 *
 * <pre>
 * PROGRAMADO ──suspender──▶ SUSPENDIDO ──reactivar──▶ PROGRAMADO
 * PROGRAMADO / SUSPENDIDO ──finalizar──▶ FINALIZADO (se puede volver a finalizar para corregir)
 * PROGRAMADO / SUSPENDIDO ──anular──▶ ANULADO (definitivo)
 * </pre>
 */
public enum EstadoEvento {
    /** Estado inicial. Admite apuestas mientras no haya empezado. */
    PROGRAMADO,
    /** Aplazado o en revisión: no admite apuestas nuevas hasta que se reactive (HU-05). */
    SUSPENDIDO,
    /** Ya tiene resultado y sus apuestas se han resuelto (HU-04). */
    FINALIZADO,
    /** Cancelado: se devuelve lo apostado y ya no puede tener resultado (HU-05). */
    ANULADO
}

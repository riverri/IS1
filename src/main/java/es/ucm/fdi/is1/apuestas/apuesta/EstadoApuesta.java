package es.ucm.fdi.is1.apuestas.apuesta;

/**
 * Estado de una {@link Apuesta}. Lo calcula la propia apuesta a partir de sus selecciones,
 * salvo {@link #CANCELADA}, que solo se alcanza a petición del usuario.
 */
public enum EstadoApuesta {
    /** Recién hecha o con selecciones pendientes; el importe está comprometido (HU-24). */
    ACTIVA,
    /** Todas las selecciones acertadas (las anuladas cuentan con cuota 1,00): se abona la ganancia (HU-25, HU-30). */
    GANADA,
    /** Ha fallado al menos una selección; en una combinada basta con una (HU-30). */
    PERDIDA,
    /** Se anularon todas sus selecciones (evento o mercado anulado): se devolvió el importe (HU-05). */
    ANULADA,
    /** Cancelada por el usuario antes de empezar el evento; se le devolvió el importe (HU-26). */
    CANCELADA
}

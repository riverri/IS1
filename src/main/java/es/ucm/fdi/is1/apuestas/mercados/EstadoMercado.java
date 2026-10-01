package es.ucm.fdi.is1.apuestas.mercados;

/** Ciclo de vida de un mercado a largo plazo (HU-45). */
public enum EstadoMercado {
    /** Admite apuestas hasta la fecha de cierre. */
    ABIERTO,
    /** El creador lo ha cerrado antes de tiempo; espera al ganador. */
    CERRADO,
    /** Tiene ganador y sus apuestas están pagadas. */
    RESUELTO,
    /** Se ha anulado y se ha devuelto el importe de sus apuestas. */
    ANULADO
}

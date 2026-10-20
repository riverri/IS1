package es.ucm.fdi.is1.apuestas.apuesta;

/** Estado de una {@link Seleccion} dentro de una apuesta. La apuesta deduce el suyo a partir de estos. */
public enum EstadoSeleccion {
    /** Aún no se conoce el resultado del evento o el ganador del mercado. */
    PENDIENTE,
    ACERTADA,
    FALLADA,
    /**
     * El evento o el mercado se anuló, o es de goles y solo se conoce el ganador (HU-52):
     * la selección cuenta con cuota 1,00 (HU-30).
     */
    ANULADA
}

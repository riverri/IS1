package es.ucm.fdi.is1.apuestas.apuesta;

public enum EstadoSeleccion {
    PENDIENTE,
    ACERTADA,
    FALLADA,
    /** El evento se anuló: la selección cuenta con cuota 1,00 (HU-30). */
    ANULADA
}

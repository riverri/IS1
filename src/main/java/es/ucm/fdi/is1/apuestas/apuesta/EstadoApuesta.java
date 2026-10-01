package es.ucm.fdi.is1.apuestas.apuesta;

public enum EstadoApuesta {
    ACTIVA,
    GANADA,
    PERDIDA,
    ANULADA,
    /** Cancelada por el usuario antes de empezar el evento; se le devolvió el importe (HU-26). */
    CANCELADA
}

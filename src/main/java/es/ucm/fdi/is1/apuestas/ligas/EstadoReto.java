package es.ucm.fdi.is1.apuestas.ligas;

/** Estado de un {@link Reto} 1 contra 1 (HU-89). */
public enum EstadoReto {
    /** Esperando a que el retado acepte o rechace. */
    PENDIENTE,
    /** Los dos han pagado; se decide con el resultado del partido. */
    ACEPTADO,
    /** El retado lo rechazó o el retador lo retiró: se devolvió el importe. */
    RECHAZADO,
    /** Uno de los dos acertó y se llevó todo. */
    RESUELTO,
    /** Nadie acertó, se anuló el partido o empezó sin aceptarse: se devolvió el importe a cada uno. */
    DEVUELTO
}

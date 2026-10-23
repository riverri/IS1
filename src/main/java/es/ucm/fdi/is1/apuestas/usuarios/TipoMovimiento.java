package es.ucm.fdi.is1.apuestas.usuarios;

/**
 * Motivo de un movimiento del saldo (HU-55). Cada valor lleva el texto que se muestra en
 * <i>Mi cuenta → Movimientos</i>. Los juegos nuevos (casino, predicciones) añadirán aquí sus tipos.
 */
public enum TipoMovimiento {

    /** Saldo con el que empieza toda cuenta (HU-14). */
    BIENVENIDA("Saldo de bienvenida"),
    /** Recarga gratuita periódica (HU-14). */
    RECARGA("Recarga gratuita"),
    /** Importe que se cobra al apostar o al subir el importe de una apuesta (HU-23, HU-27). */
    APUESTA("Apuesta"),
    /** Ganancia de una apuesta acertada (HU-25). */
    PREMIO("Premio"),
    /** Importe que se devuelve: apuesta cancelada, anulada o con el importe rebajado (HU-05, HU-26, HU-27). */
    DEVOLUCION("Devolución"),
    /** Diferencia al corregir un resultado ya pagado; puede ser negativa (HU-04, ADR 0002). */
    CORRECCION("Corrección de resultado"),
    /** El saldo pasa a cero al eliminar la cuenta (HU-18). */
    BAJA("Baja de la cuenta"),
    /** Saldo que ya tenía cada cuenta cuando se creó el libro de movimientos (migración V11). */
    SALDO_INICIAL("Saldo anterior a los movimientos");

    private final String descripcion;

    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

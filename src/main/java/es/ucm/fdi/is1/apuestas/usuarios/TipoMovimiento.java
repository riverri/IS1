package es.ucm.fdi.is1.apuestas.usuarios;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Motivo de un movimiento del saldo (HU-55). Cada valor lleva el texto que se muestra en
 * <i>Mi cuenta → Movimientos</i> y si es lo que se juega en un juego que no es una apuesta deportiva.
 *
 * <p>Los marcados como {@code juego} cuentan en los límites del juego responsable (HU-10) a través de este libro.
 * Las apuestas deportivas ({@link #APUESTA}) no se marcan porque ya se cuentan con las propias apuestas
 * ({@code ApuestaRepository.apostadoDesde}); si se marcaran, contarían dos veces.
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
    SALDO_INICIAL("Saldo anterior a los movimientos"),
    /** Lo que se juega en una partida del casino, también al doblar en el blackjack (HU-56, HU-57). */
    CASINO_APUESTA("Casino: apuesta", true),
    /** Lo que se cobra al ganar (o se recupera al empatar) una partida del casino. */
    CASINO_PREMIO("Casino: premio"),
    /** Compra de participaciones en un mercado de predicción, con la comisión (HU-64). */
    PREDICCION_COMPRA("Predicción: compra", true),
    /** Lo que se recupera al vender participaciones, ya sin la comisión (HU-65). */
    PREDICCION_VENTA("Predicción: venta"),
    /** Lo cobrado al resolverse un mercado: 1 monedita por participación ganadora (HU-66). */
    PREDICCION_PAGO("Predicción: pago"),
    /** Compra de una criptomoneda, con la comisión (HU-70). */
    CRIPTO_COMPRA("Cripto: compra", true),
    /** Lo que se recupera al vender una criptomoneda, ya sin la comisión (HU-71). */
    CRIPTO_VENTA("Cripto: venta");

    private final String descripcion;
    private final boolean juego;

    TipoMovimiento(String descripcion) {
        this(descripcion, false);
    }

    TipoMovimiento(String descripcion, boolean juego) {
        this.descripcion = descripcion;
        this.juego = juego;
    }

    /** Los tipos que cuentan como "lo jugado" en el juego responsable (HU-10). */
    public static Set<TipoMovimiento> deJuego() {
        return Arrays.stream(values()).filter(t -> t.juego)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TipoMovimiento.class)));
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isJuego() {
        return juego;
    }
}

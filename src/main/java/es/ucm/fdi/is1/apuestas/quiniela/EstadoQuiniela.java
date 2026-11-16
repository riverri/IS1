package es.ucm.fdi.is1.apuestas.quiniela;

/** Estado de una {@link Quiniela} (HU-78 a HU-80). */
public enum EstadoQuiniela {
    /** Admite boletos mientras no empiece ningún partido; luego espera a que se jueguen todos. */
    ABIERTA,
    /** El creador la anuló antes de repartir: se devolvió el precio de todos los boletos. */
    ANULADA,
    /** Se repartió el bote (o se devolvió todo si nadie acertó ninguno). */
    REPARTIDA
}

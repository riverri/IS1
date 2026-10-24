package es.ucm.fdi.is1.apuestas.anuncios;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Un anuncio con sus cifras para Gestión (HU-62): veces mostrado, clics y porcentaje de clics (CTR).
 *
 * @param clics número de veces que se ha pulsado
 */
public record EstadisticaAnuncio(Anuncio anuncio, long clics) {

    /** Clics / impresiones en %, con un decimal; null si aún no se ha mostrado. */
    public BigDecimal getPorcentajeClics() {
        if (anuncio.getImpresiones() == 0) {
            return null;
        }
        return BigDecimal.valueOf(clics * 100).divide(BigDecimal.valueOf(anuncio.getImpresiones()), 1,
                RoundingMode.HALF_UP);
    }
}

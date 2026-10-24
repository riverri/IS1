package es.ucm.fdi.is1.apuestas.cripto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Descarga los precios cada cierto tiempo (apuestas.cripto.intervalo; PT2M = cada 2 minutos) si
 * apuestas.cripto.actualizacion-automatica está activado. Las pruebas lo apagan para no depender de Internet.
 * Funciona igual que {@code api.SincronizacionProgramada}.
 */
@Component
public class PreciosProgramados {

    private static final Logger LOG = LoggerFactory.getLogger(PreciosProgramados.class);

    private final CriptoService cripto;
    private final CriptoProperties propiedades;

    public PreciosProgramados(CriptoService cripto, CriptoProperties propiedades) {
        this.cripto = cripto;
        this.propiedades = propiedades;
    }

    @Scheduled(initialDelayString = "${apuestas.cripto.retraso-inicial:PT10S}",
            fixedDelayString = "${apuestas.cripto.intervalo:PT2M}")
    public void actualizar() {
        if (!propiedades.actualizacionAutomatica()) {
            return;
        }
        try {
            cripto.actualizarPrecios();
        } catch (RuntimeException e) {
            // Sin precios al día no se compra ni se vende, pero el resto de la web sigue igual; se reintenta luego
            LOG.warn("No se han podido descargar los precios de las criptomonedas: {}", e.getMessage());
        }
    }
}

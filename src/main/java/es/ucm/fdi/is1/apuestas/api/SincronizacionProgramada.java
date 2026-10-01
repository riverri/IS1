package es.ucm.fdi.is1.apuestas.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Sincroniza con la API cada cierto tiempo (apuestas.api.intervalo), solo si hay clave configurada. */
@Component
public class SincronizacionProgramada {

    private static final Logger LOG = LoggerFactory.getLogger(SincronizacionProgramada.class);

    private final SincronizacionService sincronizacion;
    private final ApiProperties propiedades;

    public SincronizacionProgramada(SincronizacionService sincronizacion, ApiProperties propiedades) {
        this.sincronizacion = sincronizacion;
        this.propiedades = propiedades;
    }

    @Scheduled(initialDelayString = "${apuestas.api.retraso-inicial:PT30S}",
            fixedDelayString = "${apuestas.api.intervalo:PT30M}")
    public void sincronizar() {
        if (!propiedades.configurada()) {
            return;
        }
        try {
            sincronizacion.sincronizar();
        } catch (RuntimeException e) {
            LOG.error("Error al sincronizar con la API", e);
        }
    }
}

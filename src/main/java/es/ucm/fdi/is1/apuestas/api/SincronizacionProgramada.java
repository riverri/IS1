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
    private final SincronizacionPlantillas plantillas;
    private final ApiProperties propiedades;

    public SincronizacionProgramada(SincronizacionService sincronizacion, SincronizacionPlantillas plantillas,
                                    ApiProperties propiedades) {
        this.sincronizacion = sincronizacion;
        this.plantillas = plantillas;
        this.propiedades = propiedades;
    }

    /** Las plantillas cambian poco: una vez al día (y al poco de arrancar). */
    @Scheduled(initialDelayString = "${apuestas.api.retraso-inicial-plantillas:PT2M}",
            fixedDelayString = "${apuestas.api.intervalo-plantillas:P1D}")
    public void sincronizarPlantillas() {
        if (!propiedades.configurada()) {
            return;
        }
        try {
            plantillas.sincronizar();
        } catch (RuntimeException e) {
            LOG.error("Error al descargar las plantillas", e);
        }
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

package es.ucm.fdi.is1.apuestas.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sincroniza con la API cada cierto tiempo (apuestas.api.intervalo y apuestas.api.intervalo-plantillas),
 * solo si hay clave configurada.
 * Son dos tareas: partidos y resultados ({@link SincronizacionService}, cada 30 minutos) y plantillas
 * ({@link SincronizacionPlantillas}, una vez al día). Así los resultados llegan y las apuestas se pagan sin que
 * el creador tenga que hacer nada (HU-21, HU-25, HU-50).
 *
 * <p>Los tiempos van en formato ISO-8601 (PT30S = 30 segundos, PT2M = 2 minutos, PT30M = 30 minutos,
 * P1D = 1 día) y se pueden cambiar en application.properties. Con {@code fixedDelay} el intervalo se cuenta
 * desde que termina la ejecución anterior, así que una tarea nunca se solapa consigo misma.
 */
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

    /**
     * Las plantillas cambian poco: una vez al día (y al poco de arrancar).
     * {@code @Scheduled}: Spring llama solo a este método cada cierto tiempo (funciona gracias a
     * {@code @EnableScheduling} en {@code ApuestasApplication}). {@code initialDelay}: cuánto espera la primera vez.
     */
    @Scheduled(initialDelayString = "${apuestas.api.retraso-inicial-plantillas:PT2M}",
            fixedDelayString = "${apuestas.api.intervalo-plantillas:P1D}")
    public void sincronizarPlantillas() {
        // Sin clave no se hace nada (sincronizar() lanzaría una excepción cada vez)
        if (!propiedades.configurada()) {
            return;
        }
        try {
            plantillas.sincronizar();
        } catch (RuntimeException e) {
            // Se apunta en el registro; la próxima ejecución se intenta igual
            LOG.error("Error al descargar las plantillas", e);
        }
    }

    /** Partidos y resultados: 30 segundos después de arrancar y luego cada 30 minutos. */
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

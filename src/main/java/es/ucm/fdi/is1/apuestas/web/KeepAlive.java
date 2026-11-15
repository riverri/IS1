package es.ucm.fdi.is1.apuestas.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Mantiene la web despierta en Render (HU-85). El plan gratuito apaga el servidor tras 15 minutos sin peticiones,
 * y la siguiente visita tiene que esperar a que arranque otra vez (de 30 a 60 segundos). Cada
 * apuestas.keep-alive.intervalo (PT10M = cada 10 minutos) se hace un GET a /ping por la dirección pública: la
 * petición entra por Render como la de cualquier visitante y cuenta como actividad.
 *
 * <p>Si la web ya estaba apagada, esto no la despierta: solo evita que se apague mientras está encendida. Tras un
 * despliegue o un reinicio arranca sola, así que basta con eso. Funciona igual que
 * {@code cripto.PreciosProgramados}.
 */
@Component
public class KeepAlive {

    private static final Logger LOG = LoggerFactory.getLogger(KeepAlive.class);

    private final RestClient cliente;
    private final KeepAliveProperties propiedades;

    /**
     * El {@code RestClient.Builder} lo da Spring con los tiempos de espera de {@code api.TiemposDeEsperaConfig}:
     * si la web no responde, la petición no se queda colgada.
     */
    public KeepAlive(RestClient.Builder builder, KeepAliveProperties propiedades) {
        this.cliente = builder.build();
        this.propiedades = propiedades;
    }

    /**
     * Las tareas programadas comparten un hilo: si una sincronización con la API tarda, el ping espera a que
     * termine. Con 10 minutos de intervalo queda margen hasta los 15 en que Render apaga la web.
     */
    @Scheduled(initialDelayString = "${apuestas.keep-alive.intervalo:PT10M}",
            fixedDelayString = "${apuestas.keep-alive.intervalo:PT10M}")
    public void mantenerDespierta() {
        if (!propiedades.activo()) {
            return;
        }
        String direccion = propiedades.url().replaceAll("/+$", "") + "/ping";
        try {
            cliente.get().uri(direccion).retrieve().toBodilessEntity();
            LOG.debug("Keep-alive: {} responde", direccion);
        } catch (RestClientException e) {
            // Un fallo suelto no importa: el siguiente intento llega en el próximo intervalo
            LOG.warn("Keep-alive: {} no responde: {}", direccion, e.getMessage());
        }
    }
}

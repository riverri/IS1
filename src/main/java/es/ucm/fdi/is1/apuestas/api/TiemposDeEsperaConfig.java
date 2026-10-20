package es.ucm.fdi.is1.apuestas.api;

import java.time.Duration;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Tiempos de espera de las llamadas a la API: si deja de responder, la sincronización no se queda esperando
 * para siempre (las dos tareas programadas comparten hilo y se habrían parado las dos).
 */
@Configuration
public class TiemposDeEsperaConfig {

    /** Máximo para conectar con el servidor de la API. */
    static final Duration ESPERA_CONEXION = Duration.ofSeconds(5);
    /** Máximo esperando la respuesta, una vez conectados. */
    static final Duration ESPERA_RESPUESTA = Duration.ofSeconds(20);

    /**
     * Spring aplica este ajuste a los {@code RestClient.Builder} que crea, como el que recibe
     * {@link FootballDataCliente}. Si se pasa el tiempo, la llamada lanza una {@code RestClientException}
     * y la sincronización la apunta como un error más.
     */
    @Bean
    public RestClientCustomizer tiemposDeEspera() {
        return builder -> {
            SimpleClientHttpRequestFactory peticiones = new SimpleClientHttpRequestFactory();
            peticiones.setConnectTimeout(ESPERA_CONEXION);
            peticiones.setReadTimeout(ESPERA_RESPUESTA);
            builder.requestFactory(peticiones);
        };
    }
}

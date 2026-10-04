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

    static final Duration ESPERA_CONEXION = Duration.ofSeconds(5);
    static final Duration ESPERA_RESPUESTA = Duration.ofSeconds(20);

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

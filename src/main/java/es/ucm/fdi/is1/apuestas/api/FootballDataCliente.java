package es.ucm.fdi.is1.apuestas.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cliente de la API v4 de football-data.org (plan gratuito: 10 peticiones por minuto). */
@Component
public class FootballDataCliente implements FuenteDatosDeportivos {

    private final RestClient http;

    public FootballDataCliente(RestClient.Builder builder, ApiProperties propiedades) {
        this.http = builder
                .baseUrl(propiedades.url())
                .defaultHeader("X-Auth-Token", propiedades.token() == null ? "" : propiedades.token())
                .build();
    }

    @Override
    public List<PartidoApi> partidos(String competicion, LocalDate desde, LocalDate hasta) {
        PartidoApi.Respuesta respuesta = http.get()
                .uri("/competitions/{codigo}/matches?dateFrom={desde}&dateTo={hasta}", competicion, desde, hasta)
                .retrieve()
                .body(PartidoApi.Respuesta.class);
        return respuesta == null || respuesta.matches() == null ? List.of() : respuesta.matches();
    }
}

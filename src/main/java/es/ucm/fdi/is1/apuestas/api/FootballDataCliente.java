package es.ucm.fdi.is1.apuestas.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente de la API v4 de football-data.org (plan gratuito: 10 peticiones por minuto).
 * Es la implementación real de {@link FuenteDatosDeportivos}: hace peticiones GET con el {@code RestClient} de
 * Spring y convierte el JSON en {@link PartidoApi} y {@link PlantillaApi}. Los tiempos máximos de espera los pone
 * {@link TiemposDeEsperaConfig}. Si la API da error o no responde, se lanza {@code RestClientException}.
 */
@Component
public class FootballDataCliente implements FuenteDatosDeportivos {

    private final RestClient http;

    public FootballDataCliente(RestClient.Builder builder, ApiProperties propiedades) {
        // Todas las peticiones llevan la clave en la cabecera X-Auth-Token, que es donde la busca football-data.org.
        // Sin clave se manda vacía; de todos modos, los servicios no llaman a la API si no está configurada
        this.http = builder
                .baseUrl(propiedades.url())
                .defaultHeader("X-Auth-Token", propiedades.token() == null ? "" : propiedades.token())
                .build();
    }

    @Override
    public List<PartidoApi> partidos(String competicion, LocalDate desde, LocalDate hasta) {
        // Las fechas van como AAAA-MM-DD, que es lo que da LocalDate.toString().
        // get() = petición GET; uri() = dirección, con {codigo}, {desde} y {hasta} sustituidos por los parámetros;
        // retrieve() la envía; body(...) convierte el JSON de la respuesta en objetos Java (records PartidoApi)
        PartidoApi.Respuesta respuesta = http.get()
                .uri("/competitions/{codigo}/matches?dateFrom={desde}&dateTo={hasta}", competicion, desde, hasta)
                .retrieve()
                .body(PartidoApi.Respuesta.class);
        // Respuesta vacía o sin lista: no hay partidos
        return respuesta == null || respuesta.matches() == null ? List.of() : respuesta.matches();
    }

    @Override
    public List<PlantillaApi> plantillas(String competicion) {
        PlantillaApi.Respuesta respuesta = http.get()
                .uri("/competitions/{codigo}/teams", competicion)
                .retrieve()
                .body(PlantillaApi.Respuesta.class);
        return respuesta == null || respuesta.teams() == null ? List.of() : respuesta.teams();
    }
}

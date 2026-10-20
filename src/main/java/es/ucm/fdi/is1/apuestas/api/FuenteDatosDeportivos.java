package es.ucm.fdi.is1.apuestas.api;

import java.time.LocalDate;
import java.util.List;

/**
 * De dónde salen los partidos y resultados reales. Interfaz para poder sustituirla en las pruebas.
 * La implementa {@link FootballDataCliente}; en las pruebas de sincronización se usa un objeto simulado,
 * así no hace falta Internet ni la clave de la API.
 */
public interface FuenteDatosDeportivos {

    /**
     * Partidos de una competición (código de la API, p. ej. PD) entre dos fechas.
     *
     * @throws org.springframework.web.client.RestClientException si la API da error o no responde
     */
    List<PartidoApi> partidos(String competicion, LocalDate desde, LocalDate hasta);

    /**
     * Equipos de una competición con sus plantillas.
     *
     * @throws org.springframework.web.client.RestClientException si la API da error o no responde
     */
    List<PlantillaApi> plantillas(String competicion);
}

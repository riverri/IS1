package es.ucm.fdi.is1.apuestas.cripto;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la API de precios de criptomonedas (application.properties, prefijo apuestas.cripto). Spring la
 * rellena al arrancar, como {@code api.ApiProperties}. El intervalo de descarga lo lee {@link PreciosProgramados}.
 *
 * @param url                     dirección base de la API pública de CoinGecko
 * @param clave                   clave opcional del plan gratuito "Demo" (variable COINGECKO_CLAVE); sin ella también
 *                                funciona, con menos peticiones por minuto
 * @param actualizacionAutomatica si los precios se descargan solos cada cierto tiempo; las pruebas lo apagan para no
 *                                depender de Internet
 */
@ConfigurationProperties("apuestas.cripto")
public record CriptoProperties(String url, String clave, boolean actualizacionAutomatica) {
}

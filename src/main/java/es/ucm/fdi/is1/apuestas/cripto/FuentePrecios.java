package es.ucm.fdi.is1.apuestas.cripto;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * De dónde salen los precios de las criptomonedas. La implementación real es {@link CoinGeckoCliente}; las pruebas
 * ponen una falsa para no depender de Internet, como con {@code api.FuenteDatosDeportivos}.
 */
public interface FuentePrecios {

    /**
     * Precio en euros de varias criptomonedas.
     *
     * @param ids identificadores en la API ("dogecoin", "pepe"…)
     * @return por cada identificador encontrado, su precio; los que la API no conoce no aparecen
     * @throws org.springframework.web.client.RestClientException si la API da error o no responde
     */
    Map<String, Precio> precios(Collection<String> ids);

    /**
     * @param eur          precio en euros
     * @param variacion24h variación en las últimas 24 horas, en %; null si la API no la da
     */
    record Precio(BigDecimal eur, BigDecimal variacion24h) {
    }
}

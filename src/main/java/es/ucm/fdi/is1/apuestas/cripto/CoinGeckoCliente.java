package es.ucm.fdi.is1.apuestas.cripto;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente de la API pública de CoinGecko: es la implementación real de {@link FuentePrecios}. Una sola petición GET
 * trae el precio en euros y la variación en 24 horas de todas las criptomonedas. No hace falta clave; si se configura
 * una (variable COINGECKO_CLAVE, plan gratuito "Demo"), se manda en la cabecera que pide CoinGecko.
 *
 * <p>La respuesta es así: {@code {"dogecoin": {"eur": 0.1534, "eur_24h_change": -2.31}, "pepe": {...}}}.
 * Los tiempos máximos de espera los pone {@code api.TiemposDeEsperaConfig}, igual que para football-data.org.
 */
@Component
public class CoinGeckoCliente implements FuentePrecios {

    private final RestClient http;

    public CoinGeckoCliente(RestClient.Builder builder, CriptoProperties propiedades) {
        RestClient.Builder configurado = builder.baseUrl(propiedades.url());
        if (propiedades.clave() != null && !propiedades.clave().isBlank()) {
            configurado = configurado.defaultHeader("x-cg-demo-api-key", propiedades.clave());
        }
        this.http = configurado.build();
    }

    @Override
    public Map<String, Precio> precios(Collection<String> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        // El JSON se lee como un mapa de mapas: id → {"eur": precio, "eur_24h_change": variación}
        Map<String, Map<String, BigDecimal>> respuesta = http.get()
                .uri("/simple/price?ids={ids}&vs_currencies=eur&include_24hr_change=true", String.join(",", ids))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Map<String, BigDecimal>>>() {
                });
        Map<String, Precio> precios = new HashMap<>();
        if (respuesta != null) {
            respuesta.forEach((id, datos) -> {
                // Sin precio en euros no sirve para nada
                if (datos != null && datos.get("eur") != null) {
                    precios.put(id, new Precio(datos.get("eur"), datos.get("eur_24h_change")));
                }
            });
        }
        return precios;
    }
}

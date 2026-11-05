package es.ucm.fdi.is1.apuestas.cripto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Comprueba la petición a CoinGecko y la lectura de su respuesta (HU-69), sin salir a Internet. */
class CoinGeckoClienteTest {

    /** Como la de verdad: las memecoins valen fracciones de céntimo y CoinGecko las da en notación científica. */
    private static final String RESPUESTA = """
            {
              "dogecoin": {"eur": 0.1534, "eur_24h_change": -2.317},
              "pepe": {"eur": 1.234e-05, "eur_24h_change": 18.2},
              "bitcoin": {"eur": 84312.55},
              "rara": {}
            }
            """;

    @Test
    void pideTodosLosPreciosEnEurosEnUnaSolaPeticionYLeeLaRespuesta() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        CoinGeckoCliente cliente = new CoinGeckoCliente(builder,
                new CriptoProperties("https://api.coingecko.com/api/v3", "clave-demo", false));

        // Las comas entre los ids pueden ir tal cual o codificadas (%2C): CoinGecko entiende las dos
        servidor.expect(requestTo(matchesPattern("https://api\\.coingecko\\.com/api/v3/simple/price"
                        + "\\?ids=dogecoin(,|%2C)pepe(,|%2C)bitcoin(,|%2C)rara&vs_currencies=eur&include_24hr_change=true")))
                .andExpect(header("x-cg-demo-api-key", "clave-demo"))
                .andRespond(withSuccess(RESPUESTA, MediaType.APPLICATION_JSON));

        Map<String, FuentePrecios.Precio> precios = cliente.precios(List.of("dogecoin", "pepe", "bitcoin", "rara"));

        servidor.verify();
        assertThat(precios).containsOnlyKeys("dogecoin", "pepe", "bitcoin");
        assertThat(precios.get("dogecoin").eur()).isEqualByComparingTo("0.1534");
        assertThat(precios.get("dogecoin").variacion24h()).isEqualByComparingTo("-2.317");
        assertThat(precios.get("pepe").eur()).isEqualByComparingTo("0.00001234");
        // Sin variación: se lee igual
        assertThat(precios.get("bitcoin").variacion24h()).isNull();
    }

    @Test
    void sinCriptosNoHacePeticion() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        CoinGeckoCliente cliente = new CoinGeckoCliente(builder,
                new CriptoProperties("https://api.coingecko.com/api/v3", "", false));

        assertThat(cliente.precios(List.of())).isEmpty();
        servidor.verify();
    }
}

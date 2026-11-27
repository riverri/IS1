package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** RNF-01.1: el keep-alive hace un GET a /ping por la dirección pública, y sin dirección no hace nada. */
class KeepAliveTest {

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();

    @Test
    void conDireccionHaceUnGetAPing() {
        // La barra final de la dirección no se duplica
        KeepAlive keepAlive = new KeepAlive(builder, new KeepAliveProperties("https://better.onrender.com/"));
        servidor.expect(requestTo("https://better.onrender.com/ping"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("ok", null));

        keepAlive.mantenerDespierta();

        servidor.verify();
    }

    @Test
    void sinDireccionNoHaceNingunaPeticion() {
        // Así funciona en local y en las pruebas: MockRestServiceServer falla si llega una petición que no espera
        KeepAlive keepAlive = new KeepAlive(builder, new KeepAliveProperties(""));

        keepAlive.mantenerDespierta();

        servidor.verify();
    }

    @Test
    void siLaWebNoRespondeNoFalla() {
        KeepAlive keepAlive = new KeepAlive(builder, new KeepAliveProperties("https://better.onrender.com"));
        servidor.expect(requestTo("https://better.onrender.com/ping")).andRespond(withServerError());

        assertThatCode(keepAlive::mantenerDespierta).doesNotThrowAnyException();
        servidor.verify();
    }
}

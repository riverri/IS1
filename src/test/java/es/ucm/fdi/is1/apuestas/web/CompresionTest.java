package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * RNF-01.2: las páginas viajan comprimidas con gzip. La compresión la hace el servidor (Tomcat), así que esta
 * prueba arranca la aplicación de verdad en un puerto libre y le hace peticiones HTTP; MockMvc no pasa por ahí.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompresionTest {

    @LocalServerPort
    private int puerto;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void siElNavegadorAceptaGzipLaPaginaLlegaComprimida() throws Exception {
        HttpResponse<byte[]> respuesta = pedir("/", "gzip");

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.headers().firstValue("Content-Encoding")).hasValue("gzip");
        assertThat(descomprimir(respuesta.body())).contains("Better");
    }

    @Test
    void siNoLaAceptaLlegaSinComprimir() throws Exception {
        HttpResponse<byte[]> respuesta = pedir("/", null);

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.headers().firstValue("Content-Encoding")).isEmpty();
        assertThat(new String(respuesta.body(), StandardCharsets.UTF_8)).contains("Better");
    }

    private HttpResponse<byte[]> pedir(String ruta, String codificacion) throws IOException, InterruptedException {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta));
        if (codificacion != null) {
            peticion.header("Accept-Encoding", codificacion);
        }
        return http.send(peticion.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    private static String descomprimir(byte[] gzip) throws IOException {
        try (GZIPInputStream entrada = new GZIPInputStream(new ByteArrayInputStream(gzip))) {
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

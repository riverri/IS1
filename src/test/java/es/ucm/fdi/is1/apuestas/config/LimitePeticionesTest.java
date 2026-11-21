package es.ucm.fdi.is1.apuestas.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Límite de envíos por IP en registrarse y recuperar la contraseña, con un reloj que se adelanta a mano. */
class LimitePeticionesTest {

    private final AtomicReference<Instant> ahora = new AtomicReference<>(Instant.parse("2026-11-21T10:00:00Z"));
    private final Clock reloj = new Clock() {
        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora.get();
        }
    };
    private final LimitePeticiones limite = new LimitePeticiones(3, Duration.ofMinutes(15), reloj);

    private int enviar(String metodo, String ruta, String ip) throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest(metodo, ruta);
        peticion.setRemoteAddr(ip);
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        limite.doFilter(peticion, respuesta, new MockFilterChain());
        return respuesta.getStatus();
    }

    @Test
    void alCuartoEnvioEnLaVentanaResponde429() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertThat(enviar("POST", "/registro", "1.2.3.4")).isEqualTo(200);
        }
        MockHttpServletRequest peticion = new MockHttpServletRequest("POST", "/registro");
        peticion.setRemoteAddr("1.2.3.4");
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        limite.doFilter(peticion, respuesta, new MockFilterChain());
        assertThat(respuesta.getStatus()).isEqualTo(429);
        assertThat(respuesta.getHeader("Retry-After")).isEqualTo("900");
    }

    @Test
    void cadaIpYCadaFormularioCuentanAparte() throws Exception {
        for (int i = 0; i < 3; i++) {
            enviar("POST", "/registro", "1.2.3.4");
        }
        assertThat(enviar("POST", "/registro", "5.6.7.8")).isEqualTo(200);
        assertThat(enviar("POST", "/recuperar", "1.2.3.4")).isEqualTo(200);
    }

    @Test
    void verLaPaginaYOtrasRutasNoCuentan() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertThat(enviar("GET", "/registro", "1.2.3.4")).isEqualTo(200);
            assertThat(enviar("POST", "/login", "1.2.3.4")).isEqualTo(200);
            assertThat(enviar("POST", "/recuperar/nueva", "1.2.3.4")).isEqualTo(200);
        }
    }

    @Test
    void pasadaLaVentanaSePuedeVolverAEnviar() throws Exception {
        for (int i = 0; i < 3; i++) {
            enviar("POST", "/recuperar", "1.2.3.4");
            ahora.set(ahora.get().plusSeconds(60));
        }
        assertThat(enviar("POST", "/recuperar", "1.2.3.4")).isEqualTo(429);
        // El primero fue hace 15 minutos justos: ya no cuenta, pero los otros dos sí
        ahora.set(Instant.parse("2026-11-21T10:15:00Z"));
        assertThat(enviar("POST", "/recuperar", "1.2.3.4")).isEqualTo(200);
        assertThat(enviar("POST", "/recuperar", "1.2.3.4")).isEqualTo(429);
    }

    @Test
    void conMaximoCeroNoHayLimite() throws Exception {
        LimitePeticiones sinLimite = new LimitePeticiones(0, Duration.ofMinutes(15), reloj);
        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest peticion = new MockHttpServletRequest("POST", "/registro");
            MockHttpServletResponse respuesta = new MockHttpServletResponse();
            sinLimite.doFilter(peticion, respuesta, new MockFilterChain());
            assertThat(respuesta.getStatus()).isEqualTo(200);
        }
    }
}

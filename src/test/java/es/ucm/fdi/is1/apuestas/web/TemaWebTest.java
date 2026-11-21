package es.ucm.fdi.is1.apuestas.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

/** HU-94: modo oscuro, también sin iniciar sesión, recordado en una cookie. */
@SpringBootTest
@AutoConfigureMockMvc
class TemaWebTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void alPulsarLaLunaSeGuardaElModoOscuroYSeVuelveALaPagina() throws Exception {
        mvc.perform(get("/tema").header("Referer", "http://localhost/eventos"))
                .andExpect(redirectedUrl("/eventos"))
                .andExpect(cookie().value("tema", "oscuro"))
                .andExpect(cookie().maxAge("tema", 365 * 24 * 3600));
    }

    @Test
    void conLaCookieLaPaginaCargaLosColoresOscurosYOfreceVolverALosClaros() throws Exception {
        mvc.perform(get("/eventos").cookie(new Cookie("tema", "oscuro")))
                .andExpect(content().string(containsString("/css/oscuro")))
                .andExpect(content().string(containsString("Colores claros")));
        mvc.perform(get("/tema").cookie(new Cookie("tema", "oscuro")))
                .andExpect(cookie().value("tema", "claro"));
    }

    @Test
    void sinElegirNadaLaWebSaleConLosColoresClaros() throws Exception {
        mvc.perform(get("/eventos"))
                .andExpect(content().string(not(containsString("/css/oscuro"))))
                .andExpect(content().string(containsString("Modo oscuro")));
    }

    @Test
    void elBotonNoCreaSesionALosVisitantes() throws Exception {
        org.assertj.core.api.Assertions.assertThat(mvc.perform(get("/eventos")).andReturn().getRequest().getSession(false)).isNull();
    }

    /** Tras un formulario de Mi cuenta con errores, la dirección es la del POST: se vuelve a Mi cuenta (antes, 405). */
    @Test
    void desdeUnFormularioDeMiCuentaConErroresVuelveAMiCuenta() throws Exception {
        mvc.perform(get("/tema").header("Referer", "http://localhost/cuenta/limites"))
                .andExpect(redirectedUrl("/cuenta"));
    }

    @Test
    void desdeMisMovimientosVuelveAMisMovimientos() throws Exception {
        mvc.perform(get("/tema").header("Referer", "http://localhost/cuenta/movimientos"))
                .andExpect(redirectedUrl("/cuenta/movimientos"));
    }

    @Test
    void noVuelveAUnaDireccionDeOtraWeb() throws Exception {
        mvc.perform(get("/tema").header("Referer", "https://otra-web.com/robar"))
                .andExpect(redirectedUrl("/robar"));
    }
}

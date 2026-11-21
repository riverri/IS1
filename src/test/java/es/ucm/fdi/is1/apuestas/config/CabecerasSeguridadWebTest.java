package es.ucm.fdi.is1.apuestas.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Cabeceras de seguridad en todas las páginas y límite de registros por IP con la aplicación entera. */
@SpringBootTest(properties = "apuestas.limite-peticiones.maximo=2")
@AutoConfigureMockMvc
@DirtiesContext
// Los usuarios que registra se deshacen al acabar: la base de datos de las pruebas es la misma para todas
@Transactional
class CabecerasSeguridadWebTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void lasPaginasLlevanLasCabecerasDeSeguridad() throws Exception {
        for (String ruta : new String[] {"/", "/eventos", "/login"}) {
            mvc.perform(get(ruta))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Security-Policy", SeguridadConfig.CSP))
                    .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                    .andExpect(header().string("Permissions-Policy", SeguridadConfig.PERMISOS))
                    .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        }
    }

    @Test
    void laPoliticaNoPermiteScriptsDentroDeLasPaginasSalvoEnLaConsolaDeH2() throws Exception {
        org.assertj.core.api.Assertions.assertThat(SeguridadConfig.CSP).contains("script-src 'self';")
                .doesNotContain("script-src 'self' 'unsafe-inline'");
        mvc.perform(get("/h2-console/").with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user("creador@apuestas.es").roles("CREADOR")))
                .andExpect(header().string("Content-Security-Policy", SeguridadConfig.CSP_CONSOLA_H2));
        org.assertj.core.api.Assertions.assertThat(SeguridadConfig.CSP_CONSOLA_H2).contains("script-src 'self' 'unsafe-inline';");
    }

    @Test
    void laPaginaDeErrorEnsenaElCodigoDeLaPeticion() throws Exception {
        mvc.perform(get("/error").requestAttr(IdPeticion.ATRIBUTO, "abc123def456")
                        .requestAttr(jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE, 500)
                        .accept(org.springframework.http.MediaType.TEXT_HTML))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("di este código: <code>abc123def456</code>")));
    }

    @Test
    void alTercerRegistroSeguidoDesdeLaMismaIpResponde429() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/registro").with(csrf()).with(peticion -> {
                peticion.setRemoteAddr("203.0.113.7");
                return peticion;
            }).param("nombre", "Bot").param("email", "bot" + i + "@ucm.es")
                    .param("password", "clave-segura-1").param("confirmacion", "clave-segura-1"))
                    .andExpect(status().is3xxRedirection());
        }
        mvc.perform(post("/registro").with(csrf()).with(peticion -> {
            peticion.setRemoteAddr("203.0.113.7");
            return peticion;
        }).param("nombre", "Bot").param("email", "bot9@ucm.es")
                .param("password", "clave-segura-1").param("confirmacion", "clave-segura-1"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"));
    }
}

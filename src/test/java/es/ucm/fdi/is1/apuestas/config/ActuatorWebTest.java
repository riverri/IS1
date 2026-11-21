package es.ucm.fdi.is1.apuestas.config;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** /actuator/health público y sin detalles; el resto de Actuator no está publicado. Id de petición en la respuesta. */
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorWebTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void healthDiceSiLaAplicacionYSuBaseDeDatosResponden() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist())
                .andExpect(header().string(IdPeticion.CABECERA, matchesPattern("[0-9a-f]{12}")));
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void lasDemasOpcionesDeActuatorNoEstanPublicadas() throws Exception {
        for (String ruta : new String[] {"/actuator/env", "/actuator/beans", "/actuator/configprops", "/actuator/heapdump"}) {
            mvc.perform(get(ruta)).andExpect(status().isNotFound());
        }
    }

    @Test
    void sinSesionTampocoSeVenYLlevanALogin() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().is3xxRedirection());
    }

    @Test
    void lasPaginasLlevanElIdDePeticion() throws Exception {
        mvc.perform(get("/eventos").header(IdPeticion.CABECERA, "render-abc123"))
                .andExpect(header().string(IdPeticion.CABECERA, "render-abc123"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("</html>")));
    }
}

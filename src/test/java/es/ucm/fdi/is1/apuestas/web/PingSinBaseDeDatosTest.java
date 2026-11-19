package es.ucm.fdi.is1.apuestas.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import es.ucm.fdi.is1.apuestas.apuesta.LimitesService;

/**
 * HU-85 (CA-85.1): el ping responde aunque la base de datos falle. Antes los atributos comunes de las páginas
 * ({@link UsuarioActualAdvice}, que lee los límites) se calculaban también para el ping, y con la base de datos
 * caída daba error: Render habría dado la web por muerta.
 */
@SpringBootTest
@AutoConfigureMockMvc
// Contexto propio (otro bean o base de datos): se cierra al terminar para no quedarse con conexiones abiertas
@DirtiesContext
class PingSinBaseDeDatosTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private LimitesService limites;

    @Test
    void elPingRespondeAunqueLaBaseDeDatosFalle() throws Exception {
        when(limites.actuales()).thenThrow(new DataAccessResourceFailureException("base de datos caída"));
        mvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }
}

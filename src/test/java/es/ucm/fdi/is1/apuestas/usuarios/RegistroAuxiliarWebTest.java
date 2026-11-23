package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Prueba la ruta auxiliar solo cuando se habilita explícitamente. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "apuestas.registro.auxiliar-habilitado=true")
@Transactional
class RegistroAuxiliarWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Test
    void creaLaCuentaSinEnviarCorreo() throws Exception {
        mvc.perform(post("/registro-auxiliar").with(csrf())
                        .param("email", "auxiliar@ucm.es")
                        .param("nombre", "Usuario auxiliar")
                        .param("password", "auxiliar123")
                        .param("confirmacion", "auxiliar123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registrado"));

        assertThat(usuarios.findByEmail("auxiliar@ucm.es"))
                .get()
                .extracting(Usuario::isEmailVerificado)
                .isEqualTo(true);
    }

    @Test
    void muestraLaPaginaAuxiliar() throws Exception {
        mvc.perform(get("/registro-auxiliar"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Registro auxiliar")));

        mvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Registro auxiliar")));
    }
}

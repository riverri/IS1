package es.ucm.fdi.is1.apuestas.usuarios;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** N-10: al cambiar la contraseña se cierran las sesiones abiertas en otros navegadores. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SesionesWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarios;

    private MockHttpSession entrar(String email, String password) throws Exception {
        return (MockHttpSession) mvc.perform(formLogin("/login").userParameter("email").user(email).password(password))
                .andReturn().getRequest().getSession(false);
    }

    @Test
    void cambiarLaContrasenaCierraLasOtrasSesiones() throws Exception {
        usuarios.crear("sesiones@ucm.es", "Sesiones", "secreta123", Rol.USUARIO);
        MockHttpSession movil = entrar("sesiones@ucm.es", "secreta123");
        MockHttpSession portatil = entrar("sesiones@ucm.es", "secreta123");

        mvc.perform(post("/cuenta/password").session(movil).with(csrf())
                        .param("actual", "secreta123").param("nueva", "otraClave456").param("confirmacion", "otraClave456"))
                .andExpect(redirectedUrl("/cuenta"));

        mvc.perform(get("/cuenta").session(portatil)).andExpect(redirectedUrl("/login?expirada"));
        mvc.perform(get("/cuenta").session(movil)).andExpect(status().isOk());
    }
}

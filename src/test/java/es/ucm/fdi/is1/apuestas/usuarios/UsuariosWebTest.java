package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** HU-11, HU-12, HU-13, HU-14: registro, inicio de sesión y saldo. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuariosWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Test
    void registroSolicitaCorreoSinCrearLaCuenta() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("email", "Ana@UCM.es")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?verificacionPendiente"));

        assertThat(usuarios.findByEmail("ana@ucm.es")).isEmpty();
    }

    /** Al cerrar sesión se vuelve a la portada y se dice que se ha cerrado (antes el aviso no salía nunca). */
    @Test
    void alCerrarSesionLaPortadaLoDice() throws Exception {
        mvc.perform(post("/logout").with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user("usuario@apuestas.es")).with(csrf()))
                .andExpect(redirectedUrl("/?logout"));
        mvc.perform(get("/?logout")).andExpect(content().string(containsString("Has cerrado sesión")));
    }

    @Test
    void registroConEmailExistenteDevuelveLaMismaRespuestaGenerica() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("email", "usuario@apuestas.es")
                )
                .andExpect(redirectedUrl("/login?verificacionPendiente"));
    }

    /** CA-11.3: un nombre con palabras ofensivas no se acepta, aunque cambie letras por números. */
    @Test
    void registroConNombreOfensivoNoCreaLaCuenta() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("email", "nuevo@ucm.es"))
                .andExpect(status().is3xxRedirection());
        assertThat(usuarios.findByEmail("nuevo@ucm.es")).isEmpty();
    }

    @Test
    void registroConContrasenasDistintasAvisa() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("email", "no-es-email"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El email no es válido")));

        assertThat(usuarios.existsByEmail("no-es-email")).isFalse();
    }

    @Test
    void loginCorrectoDaAcceso() throws Exception {
        mvc.perform(formLogin("/login").userParameter("email").user("usuario@apuestas.es").password("usuario123"))
                .andExpect(authenticated().withUsername("usuario@apuestas.es"));
    }

    @Test
    void loginIncorrectoDeniegaConMensajeGenerico() throws Exception {
        mvc.perform(formLogin("/login").userParameter("email").user("usuario@apuestas.es").password("mal"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));

        mvc.perform(get("/login").param("error", ""))
                .andExpect(content().string(containsString("Email o contraseña incorrectos")));
    }

    @Test
    @WithMockUser(username = "usuario@apuestas.es")
    void laCuentaMuestraElSaldo() throws Exception {
        mvc.perform(get("/cuenta"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1.000,00 monedas")));
    }

    @Test
    void laCuentaExigeSesion() throws Exception {
        mvc.perform(get("/cuenta")).andExpect(status().is3xxRedirection());
    }
}

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
    void registroCreaLaCuentaConElSaldoDeBienvenida() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana")
                        .param("email", "Ana@UCM.es")
                        .param("password", "secreta123")
                        .param("confirmacion", "secreta123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registrado"));

        Usuario ana = usuarios.findByEmail("ana@ucm.es").orElseThrow();
        assertThat(ana.getSaldo()).isEqualByComparingTo("1000");
        assertThat(ana.getPasswordHash()).isNotEqualTo("secreta123");
        assertThat(ana.getRol()).isEqualTo(Rol.USUARIO);
    }

    @Test
    void registroConEmailExistenteAvisa() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Otro")
                        .param("email", "usuario@apuestas.es")
                        .param("password", "secreta123")
                        .param("confirmacion", "secreta123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe una cuenta con ese email")));
    }

    @Test
    void registroConContrasenasDistintasAvisa() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Luis")
                        .param("email", "luis@ucm.es")
                        .param("password", "secreta123")
                        .param("confirmacion", "otra12345"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Las contraseñas no coinciden")));

        assertThat(usuarios.existsByEmail("luis@ucm.es")).isFalse();
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

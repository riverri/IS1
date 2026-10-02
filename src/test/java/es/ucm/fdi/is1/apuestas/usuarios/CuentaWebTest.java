package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

/** HU-47: cambiar nombre y contraseña desde Mi cuenta. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CuentaWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void crearUsuaria() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
    }

    private Usuario ana() {
        return usuarios.findByEmail(ANA).orElseThrow();
    }

    @Test
    void laCuentaMuestraLosFormularios() throws Exception {
        mvc.perform(get("/cuenta").with(COMO_ANA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cambiar nombre")))
                .andExpect(content().string(containsString("Cambiar contraseña")))
                .andExpect(content().string(containsString("value=\"Ana\"")));
    }

    @Test
    void cambiaElNombre() throws Exception {
        mvc.perform(post("/cuenta/nombre").with(COMO_ANA).with(csrf()).param("nombre", "  Ana García "))
                .andExpect(redirectedUrl("/cuenta"));

        assertThat(ana().getNombre()).isEqualTo("Ana García");
    }

    @Test
    void elNombreNoPuedeQuedarVacio() throws Exception {
        mvc.perform(post("/cuenta/nombre").with(COMO_ANA).with(csrf()).param("nombre", " "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Introduce tu nombre")));
        assertThat(ana().getNombre()).isEqualTo("Ana");
    }

    @Test
    void cambiaLaContrasenaConLaActualCorrecta() throws Exception {
        mvc.perform(post("/cuenta/password").with(COMO_ANA).with(csrf())
                        .param("actual", "secreta123").param("nueva", "nuevaClave99").param("confirmacion", "nuevaClave99"))
                .andExpect(redirectedUrl("/cuenta"));

        assertThat(passwordEncoder.matches("nuevaClave99", ana().getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("secreta123", ana().getPasswordHash())).isFalse();
    }

    @Test
    void rechazaContrasenaActualIncorrectaCortaONoCoincidente() throws Exception {
        mvc.perform(post("/cuenta/password").with(COMO_ANA).with(csrf())
                        .param("actual", "otra").param("nueva", "nuevaClave99").param("confirmacion", "nuevaClave99"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La contraseña actual no es correcta")));
        mvc.perform(post("/cuenta/password").with(COMO_ANA).with(csrf())
                        .param("actual", "secreta123").param("nueva", "corta").param("confirmacion", "corta"))
                .andExpect(content().string(containsString("entre 8 y 72 caracteres")));
        mvc.perform(post("/cuenta/password").with(COMO_ANA).with(csrf())
                        .param("actual", "secreta123").param("nueva", "nuevaClave99").param("confirmacion", "otraClave99"))
                .andExpect(content().string(containsString("Las contraseñas no coinciden")));

        assertThat(passwordEncoder.matches("secreta123", ana().getPasswordHash())).isTrue();
    }

    @Test
    void sinSesionNoSeCambiaNada() throws Exception {
        mvc.perform(post("/cuenta/nombre").with(csrf()).param("nombre", "Pirata"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}

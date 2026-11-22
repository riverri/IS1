package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Email verification tests cover registration, single-use links, and login restrictions. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VerificacionCorreoWebTest {

    private static final String EMAIL = "verificacion@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EnviosVerificacion envios;

    @MockitoBean
    private EnvioCorreo correo;

    @Test
    void elRegistroExigeConfirmarElEnlaceAntesDeIniciarSesion() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana")
                        .param("email", EMAIL)
                        .param("password", "secreta123")
                        .param("confirmacion", "secreta123"))
                .andExpect(redirectedUrl("/login?verificacionPendiente"));

        Usuario usuario = usuarios.findByEmail(EMAIL).orElseThrow();
        assertThat(usuario.isEmailVerificado()).isFalse();
        mvc.perform(formLogin("/login").userParameter("email").user(EMAIL).password("secreta123"))
                .andExpect(unauthenticated());

        org.mockito.ArgumentCaptor<String> enlace = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(correo).enviarVerificacion(eq(EMAIL), eq("Ana"), enlace.capture());
        String token = token(enlace.getValue());

        mvc.perform(get("/verificar-correo/confirmar").param("token", token))
                .andExpect(status().isOk());
        assertThat(usuarios.findByEmail(EMAIL).orElseThrow().isEmailVerificado()).isFalse();

        mvc.perform(post("/verificar-correo/confirmar").with(csrf()).param("token", token))
                .andExpect(redirectedUrl("/login?verificado"));
        assertThat(usuarios.findByEmail(EMAIL).orElseThrow().isEmailVerificado()).isTrue();
        mvc.perform(formLogin("/login").userParameter("email").user(EMAIL).password("secreta123"))
                .andExpect(org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers
                        .authenticated().withUsername(EMAIL));

        mvc.perform(post("/verificar-correo/confirmar").with(csrf()).param("token", token))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void elReenvioNoRevelaSiExisteLaCuentaYRespetaElLimite() throws Exception {
        RegistroForm form = registro("Reenvio", EMAIL);
        usuarioService.registrar(form);
        envios.olvidar(EMAIL);

        for (int i = 0; i < EnviosVerificacion.MAXIMO + 1; i++) {
            mvc.perform(post("/verificar-correo").with(csrf()).param("email", EMAIL))
                    .andExpect(redirectedUrl("/verificar-correo"));
        }
        verify(correo, org.mockito.Mockito.times(EnviosVerificacion.MAXIMO))
                .enviarVerificacion(eq(EMAIL), eq("Reenvio"), any());

        mvc.perform(post("/verificar-correo").with(csrf()).param("email", "inexistente@ucm.es"))
                .andExpect(redirectedUrl("/verificar-correo"))
                .andExpect(status().is3xxRedirection());
    }

    private static String token(String enlace) {
        return enlace.substring(enlace.indexOf("token=") + "token=".length());
    }

    private static RegistroForm registro(String nombre, String email) {
        RegistroForm form = new RegistroForm();
        form.setNombre(nombre);
        form.setEmail(email);
        form.setPassword("secreta123");
        form.setConfirmacion("secreta123");
        return form;
    }
}

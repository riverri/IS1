package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/** Registration tests cover email proof, account creation timing, single-use tokens, and login restrictions. */
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
    void laCuentaNoExisteHastaConfirmarCorreoYCompletarCredenciales() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("email", EMAIL))
                .andExpect(redirectedUrl("/login?verificacionPendiente"));
        assertThat(usuarios.findByEmail(EMAIL)).isEmpty();

        ArgumentCaptor<String> enlace = ArgumentCaptor.forClass(String.class);
        verify(correo).enviarVerificacion(eq(EMAIL), eq(""), enlace.capture());
        String token = token(enlace.getValue());

        mvc.perform(get("/registro/confirmar").param("token", token)).andExpect(status().isOk());
        assertThat(usuarios.findByEmail(EMAIL)).isEmpty();

        MvcResult confirmacion = mvc.perform(post("/registro/confirmar").with(csrf()).param("token", token))
                .andExpect(redirectedUrl("/registro/completar"))
                .andReturn();
        MockHttpSession sesion = (MockHttpSession) confirmacion.getRequest().getSession(false);
        assertThat(sesion).isNotNull();
        assertThat(usuarios.findByEmail(EMAIL)).isEmpty();

        mvc.perform(post("/registro/completar").session(sesion).with(csrf())
                        .param("nombre", "Ana")
                        .param("password", "secreta123")
                        .param("confirmacion", "secreta123"))
                .andExpect(redirectedUrl("/login?registrado"));

        Usuario usuario = usuarios.findByEmail(EMAIL).orElseThrow();
        assertThat(usuario.isEmailVerificado()).isTrue();
        assertThat(usuario.getNombre()).isEqualTo("Ana");
        assertThat(usuario.getSaldo()).isEqualByComparingTo("1000");
        assertThat(usuario.getPasswordHash()).isNotEqualTo("secreta123");
        assertThat(usuario.getRol()).isEqualTo(Rol.USUARIO);
        mvc.perform(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders
                        .formLogin("/login").userParameter("email").user(EMAIL).password("secreta123"))
                .andExpect(org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers
                        .authenticated().withUsername(EMAIL));

        mvc.perform(post("/registro/confirmar").with(csrf()).param("token", token))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void registroPendienteReenviaEnlaceConLimiteSinCrearCuenta() throws Exception {
        envios.olvidar(EMAIL);
        for (int i = 0; i < EnviosVerificacion.MAXIMO + 1; i++) {
            mvc.perform(post("/registro").with(csrf()).param("email", EMAIL))
                    .andExpect(redirectedUrl("/login?verificacionPendiente"));
        }
        verify(correo, org.mockito.Mockito.times(EnviosVerificacion.MAXIMO))
                .enviarVerificacion(eq(EMAIL), eq(""), any());
        assertThat(usuarios.findByEmail(EMAIL)).isEmpty();
    }

    @Test
    void correoExistenteTieneLaMismaRespuestaYNoSeEnvianEnlaces() throws Exception {
        usuarioService.crear(EMAIL, "Ana", "secreta123", Rol.USUARIO);
        mvc.perform(post("/registro").with(csrf()).param("email", EMAIL))
                .andExpect(redirectedUrl("/login?verificacionPendiente"));
        org.mockito.Mockito.verifyNoInteractions(correo);
    }

    private static String token(String enlace) {
        return enlace.substring(enlace.indexOf("token=") + "token=".length());
    }
}

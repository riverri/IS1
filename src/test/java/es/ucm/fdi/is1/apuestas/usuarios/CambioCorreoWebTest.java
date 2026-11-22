package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

/** Email changes require current-password reauthentication and new-address confirmation. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CambioCorreoWebTest {

    private static final String CURRENT_EMAIL = "cambio@ucm.es";
    private static final String NEW_EMAIL = "nuevo-cambio@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private UsuarioRepository repositorio;

    @MockitoBean
    private EnvioCorreo correo;

    @Test
    void correoSoloCambiaTrasConfirmarElEnlaceEnviadoALaNuevaDireccion() throws Exception {
        usuarios.crear(CURRENT_EMAIL, "Ana", "secreta123", Rol.USUARIO);

        mvc.perform(post("/cuenta/correo").with(user(CURRENT_EMAIL)).with(csrf())
                        .param("nuevoEmail", NEW_EMAIL).param("password", "secreta123"))
                .andExpect(redirectedUrl("/cuenta"));

        assertThat(repositorio.findByEmail(CURRENT_EMAIL)).isPresent();
        assertThat(repositorio.findByEmail(NEW_EMAIL)).isEmpty();
        org.mockito.ArgumentCaptor<String> enlace = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(correo).enviarConfirmacionCambio(eq(NEW_EMAIL), eq("Ana"), enlace.capture());
        verify(correo).notificarCambioSolicitado(CURRENT_EMAIL, "Ana", NEW_EMAIL);
        String token = token(enlace.getValue());

        mvc.perform(get("/cuenta/correo/confirmar").param("token", token))
                .andExpect(status().isOk());
        assertThat(repositorio.findByEmail(CURRENT_EMAIL)).isPresent();

        mvc.perform(post("/cuenta/correo/confirmar").with(csrf()).param("token", token))
                .andExpect(redirectedUrl("/login?correoCambiado"));
        assertThat(repositorio.findByEmail(CURRENT_EMAIL)).isEmpty();
        assertThat(repositorio.findByEmail(NEW_EMAIL)).get().extracting(Usuario::isEmailVerificado).isEqualTo(true);
    }

    @Test
    void unaContrasenaIncorrectaNoSolicitaElCambio() throws Exception {
        usuarios.crear(CURRENT_EMAIL, "Ana", "secreta123", Rol.USUARIO);

        mvc.perform(post("/cuenta/correo").with(user(CURRENT_EMAIL)).with(csrf())
                        .param("nuevoEmail", NEW_EMAIL).param("password", "incorrecta"))
                .andExpect(status().isOk());

        assertThat(repositorio.findByEmail(CURRENT_EMAIL)).isPresent();
        assertThat(repositorio.findByEmail(NEW_EMAIL)).isEmpty();
        org.mockito.Mockito.verifyNoInteractions(correo);
    }

    private static String token(String enlace) {
        return enlace.substring(enlace.indexOf("token=") + "token=".length());
    }
}

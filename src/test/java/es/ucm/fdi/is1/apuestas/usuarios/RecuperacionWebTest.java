package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;

/** HU-17: recuperar la contraseña con un enlace que caduca y solo sirve una vez. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecuperacionWebTest {

    private static final String ANA = "ana@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private TokenRecuperacionRepository tokens;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EnvioCorreo correo;

    @BeforeEach
    void crearUsuaria() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
    }

    /** Pide el enlace y devuelve el código que se ha enviado por correo. */
    private String pedirEnlace() throws Exception {
        mvc.perform(post("/recuperar").with(csrf()).param("email", "Ana@UCM.es"))
                .andExpect(redirectedUrl("/recuperar?enviado"));
        ArgumentCaptor<String> enlace = ArgumentCaptor.forClass(String.class);
        verify(correo).enviarRecuperacion(eq(ANA), eq("Ana"), enlace.capture());
        assertThat(enlace.getValue()).contains("/recuperar/nueva?token=");
        return enlace.getValue().substring(enlace.getValue().indexOf("token=") + "token=".length());
    }

    private boolean contrasena(String password) {
        return passwordEncoder.matches(password, usuarios.findByEmail(ANA).orElseThrow().getPasswordHash());
    }

    @Test
    void elLoginEnlazaARecuperarLaContrasena() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(content().string(containsString("href=\"/recuperar\"")));
        mvc.perform(get("/recuperar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Recuperar la contraseña")));
    }

    @Test
    void conElEnlaceSeCreaUnaContrasenaNueva() throws Exception {
        String token = pedirEnlace();

        mvc.perform(get("/recuperar/nueva").param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Guardar la contraseña")));
        mvc.perform(post("/recuperar/nueva").with(csrf())
                        .param("token", token).param("nueva", "otraClave99").param("confirmacion", "otraClave99"))
                .andExpect(redirectedUrl("/login?recuperada"));

        assertThat(contrasena("otraClave99")).isTrue();
        assertThat(contrasena("secreta123")).isFalse();
    }

    @Test
    void elEnlaceSoloSirveUnaVez() throws Exception {
        String token = pedirEnlace();
        mvc.perform(post("/recuperar/nueva").with(csrf())
                .param("token", token).param("nueva", "otraClave99").param("confirmacion", "otraClave99"));

        mvc.perform(get("/recuperar/nueva").param("token", token))
                .andExpect(content().string(containsString("El enlace ha caducado o ya se ha usado")));
        mvc.perform(post("/recuperar/nueva").with(csrf())
                        .param("token", token).param("nueva", "terceraClave1").param("confirmacion", "terceraClave1"))
                .andExpect(content().string(containsString("El enlace ha caducado o ya se ha usado")));
        assertThat(contrasena("otraClave99")).isTrue();
    }

    @Test
    void unEnlaceCaducadoNoSirve() throws Exception {
        Usuario ana = usuarios.findByEmail(ANA).orElseThrow();
        tokens.save(new TokenRecuperacion(ana, RecuperacionService.resumen("caducado"), Hora.ahora().minusMinutes(1)));

        mvc.perform(post("/recuperar/nueva").with(csrf())
                        .param("token", "caducado").param("nueva", "otraClave99").param("confirmacion", "otraClave99"))
                .andExpect(content().string(containsString("El enlace ha caducado o ya se ha usado")));
        assertThat(contrasena("secreta123")).isTrue();
    }

    @Test
    void pedirOtroEnlaceAnulaElAnterior() throws Exception {
        String primero = pedirEnlace();
        org.mockito.Mockito.clearInvocations(correo);
        pedirEnlace();

        mvc.perform(get("/recuperar/nueva").param("token", primero))
                .andExpect(content().string(containsString("El enlace ha caducado o ya se ha usado")));
    }

    @Test
    void unEmailSinCuentaRecibeLaMismaRespuestaYNoSeEnviaNada() throws Exception {
        mvc.perform(post("/recuperar").with(csrf()).param("email", "nadie@ucm.es"))
                .andExpect(redirectedUrl("/recuperar?enviado"));
        verify(correo, never()).enviarRecuperacion(any(), any(), any());
    }

    @Test
    void laContrasenaNuevaTieneLasMismasReglasQueElRegistro() throws Exception {
        String token = pedirEnlace();

        mvc.perform(post("/recuperar/nueva").with(csrf())
                        .param("token", token).param("nueva", "corta").param("confirmacion", "corta"))
                .andExpect(content().string(containsString("entre 8 y 72 caracteres")));
        mvc.perform(post("/recuperar/nueva").with(csrf())
                        .param("token", token).param("nueva", "otraClave99").param("confirmacion", "distinta99"))
                .andExpect(content().string(containsString("Las contraseñas no coinciden")));
        assertThat(contrasena("secreta123")).isTrue();
    }
}

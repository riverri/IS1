package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Sexta depuración: cambio de correo, recuperación y navegadores recordados (#166). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CorreoYNavegadoresSeguridadWebTest {

    private static final String ANA = "seis-ana@ucm.es";
    private static final String NUEVO = "seis-nuevo@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private UsuarioRepository repositorio;

    @Autowired
    private JdbcNavegadorTokenRepository navegadores;

    @Autowired
    private EnviosVerificacion envios;

    @MockitoBean
    private EnvioCorreo correo;

    /** El límite de envíos se guarda en memoria y lo comparten las pruebas: se empieza de cero. */
    @BeforeEach
    void olvidarEnvios() {
        for (String email : List.of(ANA, "cambio:" + ANA, NUEVO, "atacante@ucm.es", "victima@ucm.es")) {
            envios.olvidar(email);
        }
    }

    private static String codigo(String enlace) {
        return enlace.substring(enlace.indexOf("token=") + "token=".length());
    }

    private String pedirCambio(String nuevo) throws Exception {
        mvc.perform(post("/cuenta/correo").with(user(ANA)).with(csrf())
                .param("nuevoEmail", nuevo).param("password", "secreta123")).andExpect(redirectedUrl("/cuenta"));
        ArgumentCaptor<String> enlace = ArgumentCaptor.forClass(String.class);
        verify(correo, atLeastOnce()).enviarConfirmacionCambio(eq(nuevo), anyString(), enlace.capture());
        return codigo(enlace.getValue());
    }

    @Test
    void cambiarLaContrasenaAnulaElCambioDeCorreoPendiente() throws Exception {
        // Alguien que sabe la contraseña pide pasar la cuenta a su correo; la dueña cambia la contraseña
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        String codigo = pedirCambio("atacante@ucm.es");
        mvc.perform(post("/cuenta/password").with(user(ANA)).with(csrf())
                        .param("actual", "secreta123").param("nueva", "otra12345").param("confirmacion", "otra12345"))
                .andExpect(redirectedUrl("/cuenta"));

        mvc.perform(post("/cuenta/correo/confirmar").with(csrf()).param("token", codigo));
        assertThat(repositorio.findByEmail(ANA)).as("la cuenta sigue con su correo").isPresent();
        assertThat(repositorio.findByEmail("atacante@ucm.es")).isEmpty();
    }

    @Test
    void elEnlaceDeRecuperacionDelCorreoAntiguoDejaDeServirAlCambiarlo() throws Exception {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        mvc.perform(post("/recuperar").with(csrf()).param("email", ANA));
        ArgumentCaptor<String> recuperacion = ArgumentCaptor.forClass(String.class);
        verify(correo).enviarRecuperacion(eq(ANA), anyString(), recuperacion.capture());
        mvc.perform(post("/cuenta/correo/confirmar").with(csrf()).param("token", pedirCambio(NUEVO)))
                .andExpect(redirectedUrl("/login?correoCambiado"));

        // Quien tenga el buzón antiguo ya no puede poner una contraseña nueva
        mvc.perform(post("/recuperar/nueva").with(csrf()).param("token", codigo(recuperacion.getValue()))
                .param("nueva", "atacante123").param("confirmacion", "atacante123"));
        assertThat(usuarios.passwordCorrecta(NUEVO, "secreta123")).isTrue();
    }

    @Test
    void seRevocaUnNavegadorAunqueSuSerieLleveBarras() throws Exception {
        // Spring genera la serie en Base64 normal: puede llevar "/", y en la ruta daba 404
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        navegadores.createNewToken(new PersistentRememberMeToken(ANA, "Ab/cD+eF12345678901234==", "t", new Date()));
        mvc.perform(get("/cuenta/navegadores").with(user(ANA)));
        mvc.perform(post("/cuenta/navegadores/revocar").with(user(ANA)).with(csrf())
                        .param("serie", "Ab/cD+eF12345678901234=="))
                .andExpect(redirectedUrl("/cuenta/navegadores"));
        assertThat(navegadores.findByUsername(ANA)).isEmpty();
    }

    @Test
    void elCambioDeCorreoTieneLimiteDeEnvios() throws Exception {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/cuenta/correo").with(user(ANA)).with(csrf())
                    .param("nuevoEmail", "victima@ucm.es").param("password", "secreta123"));
        }
        verify(correo, times(EnviosVerificacion.MAXIMO)).enviarConfirmacionCambio(eq("victima@ucm.es"), any(), any());
    }

    @Test
    void elCorreoNuevoNoPuedeSerDelDominioReservado() throws Exception {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        mvc.perform(post("/cuenta/correo").with(user(ANA)).with(csrf())
                .param("nuevoEmail", "eliminado-1@apuestas.invalid").param("password", "secreta123"));
        verify(correo, times(0)).enviarConfirmacionCambio(eq("eliminado-1@apuestas.invalid"), any(), any());
    }

    @Test
    void recuperarLaContrasenaActivaUnaCuentaSinVerificar() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("nombre", "Ana").param("email", ANA)
                .param("password", "secreta123").param("confirmacion", "secreta123"));
        mvc.perform(post("/recuperar").with(csrf()).param("email", ANA));
        ArgumentCaptor<String> recuperacion = ArgumentCaptor.forClass(String.class);
        verify(correo).enviarRecuperacion(eq(ANA), anyString(), recuperacion.capture());
        mvc.perform(post("/recuperar/nueva").with(csrf()).param("token", codigo(recuperacion.getValue()))
                        .param("nueva", "nueva12345").param("confirmacion", "nueva12345"))
                .andExpect(redirectedUrl("/login?recuperada"));

        // El enlace llegó a su correo: ya puede entrar
        mvc.perform(formLogin("/login").userParameter("email").user(ANA).password("nueva12345"))
                .andExpect(redirectedUrl("/eventos"));
    }
}

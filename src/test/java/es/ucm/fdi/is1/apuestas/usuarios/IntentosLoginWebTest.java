package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** HU-12: tras 5 contraseñas mal seguidas la cuenta se bloquea 15 minutos. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IntentosLoginWebTest {

    private static final String EVA = "eva.bloqueo@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private IntentosLogin intentos;

    @BeforeEach
    void crearUsuaria() {
        usuarioService.crear(EVA, "Eva", "secreta123", Rol.USUARIO);
        intentos.olvidar(EVA);
    }

    @AfterEach
    void olvidar() {
        // Los fallos están en memoria y se comparten con otras pruebas
        intentos.olvidar(EVA);
    }

    private void entrar(String email, String password, String destino) throws Exception {
        mvc.perform(formLogin("/login").userParameter("email").user(email).password(password))
                .andExpect(redirectedUrl(destino));
    }

    @Test
    void tras5FallosNoEntraNiConLaContrasenaBuena() throws Exception {
        for (int i = 0; i < IntentosLogin.MAXIMO_FALLOS; i++) {
            entrar(EVA, "mala", "/login?error");
        }
        assertThat(intentos.bloqueado(EVA)).isTrue();

        entrar(EVA, "secreta123", "/login?error");
    }

    @Test
    void menosDe5FallosNoBloquean() throws Exception {
        for (int i = 0; i < IntentosLogin.MAXIMO_FALLOS - 1; i++) {
            entrar(EVA, "mala", "/login?error");
        }
        entrar(EVA, "secreta123", "/eventos");
        assertThat(intentos.bloqueado(EVA)).isFalse();
    }

    /** Probar miles de emails inventados no llena la memoria: solo se apuntan las cuentas que existen. */
    @Test
    void losEmailsSinCuentaNoSeApuntan() throws Exception {
        int antes = intentos.vigilados();
        for (int i = 0; i < 20; i++) {
            entrar("inventado" + i + "@ucm.es", "mala", "/login?error");
        }
        assertThat(intentos.vigilados()).isLessThanOrEqualTo(antes);
    }
}

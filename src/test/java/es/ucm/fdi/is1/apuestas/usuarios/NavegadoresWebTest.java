package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Persistent browser login and per-device revocation coverage. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NavegadoresWebTest {

    private static final String EMAIL = "recordar@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private JdbcNavegadorTokenRepository tokens;

    @Autowired
    private NavegadorRememberMeServices rememberMe;

    @BeforeEach
    void preparar() {
        usuarios.crear(EMAIL, "Navegador", "secreta123", Rol.USUARIO);
    }

    @Test
    void loginCreaCookiePersistenteSoloCuandoSeSolicita() throws Exception {
        var login = mvc.perform(post("/login").with(csrf()).param("email", EMAIL).param("password", "secreta123")
                        .param("recordar", "true"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(tokens.findByUsername(EMAIL)).hasSize(1);

        mvc.perform(post("/logout").with(csrf()).session((MockHttpSession) login.getRequest().getSession(false))
                        .cookie(login.getResponse().getCookie("remember-me")))
                .andExpect(status().is3xxRedirection());
        assertThat(tokens.findByUsername(EMAIL)).isEmpty();

        mvc.perform(post("/login").with(csrf()).param("email", EMAIL).param("password", "secreta123"))
                .andExpect(status().is3xxRedirection());
        assertThat(tokens.findByUsername(EMAIL)).isEmpty();
    }

    @Test
    void puedeListarYRevocarUnSoloNavegador() throws Exception {
        crearToken("serie-uno", "Firefox");
        crearToken("serie-dos", "Chrome");

        mvc.perform(get("/cuenta/navegadores").with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Firefox")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Chrome")));

        mvc.perform(post("/cuenta/navegadores/revocar").param("serie", "serie-uno")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(EMAIL))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cuenta/navegadores"));

        assertThat(tokens.findByUsername(EMAIL)).extracting(NavegadorRecordado::serie)
                .containsExactly("serie-dos");
    }

    @Test
    void logoutRevocaSoloLaCookieDelNavegadorActual() {
        crearToken("serie-uno", "Firefox");
        crearToken("serie-dos", "Chrome");
        String cookie = Base64.getEncoder().encodeToString("serie-uno:token-uno".getBytes(StandardCharsets.UTF_8));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("remember-me", cookie));

        rememberMe.logout(request, new MockHttpServletResponse(),
                new TestingAuthenticationToken(EMAIL, "unused", "ROLE_USUARIO"));

        assertThat(tokens.findByUsername(EMAIL)).extracting(NavegadorRecordado::serie)
                .containsExactly("serie-dos");
    }

    private void crearToken(String series, String userAgent) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", userAgent);
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request));
        tokens.createNewToken(new PersistentRememberMeToken(EMAIL, series, "token-" + series, new Date()));
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }
}

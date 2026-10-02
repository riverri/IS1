package es.ucm.fdi.is1.apuestas.ligas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-51: ligas privadas con código de invitación y ranking propio. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LigasWebTest {

    private static final RequestPostProcessor ANA = user("ana@ucm.es");
    private static final RequestPostProcessor LUIS = user("luis@ucm.es");
    private static final RequestPostProcessor EVA = user("eva@ucm.es");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private LigaRepository ligas;

    @BeforeEach
    void preparar() {
        usuarioService.crear("ana@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear("luis@ucm.es", "Luis", "secreta123", Rol.USUARIO);
        usuarioService.crear("eva@ucm.es", "Eva", "secreta123", Rol.USUARIO);
        usuarios.findByEmail("luis@ucm.es").orElseThrow().abonar(new BigDecimal("500"));
    }

    private Liga crear(String nombre) throws Exception {
        mvc.perform(post("/ligas").with(ANA).with(csrf()).param("nombre", nombre))
                .andExpect(redirectedUrlPattern("/ligas/*"));
        return ligas.findAll().stream().filter(l -> l.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    @Test
    void alCrearlaSeGeneraUnCodigoYQuienLaCreaEsMiembro() throws Exception {
        Liga liga = crear("Los de IS1");

        assertThat(liga.getCodigo()).matches("[A-Z2-9]{6}");
        mvc.perform(get("/ligas/{id}", liga.getId()).with(ANA))
                .andExpect(content().string(containsString(liga.getCodigo())))
                .andExpect(content().string(containsString("Ana")));
        mvc.perform(get("/ligas").with(ANA))
                .andExpect(content().string(containsString("Los de IS1")));
    }

    @Test
    void conElCodigoSeEntraYElRankingEsSoloDeSusMiembros() throws Exception {
        Liga liga = crear("Los de IS1");
        String escrito = " " + liga.getCodigo().toLowerCase().substring(0, 3) + " " + liga.getCodigo().substring(3);

        mvc.perform(post("/ligas/unirse").with(LUIS).with(csrf()).param("codigo", escrito))
                .andExpect(redirectedUrl("/ligas/" + liga.getId()));

        String pagina = mvc.perform(get("/ligas/{id}", liga.getId()).with(LUIS))
                .andExpect(content().string(not(containsString(">Eva<"))))
                .andReturn().getResponse().getContentAsString();
        assertThat(pagina.indexOf(">Luis<")).as("Luis tiene más saldo y va primero")
                .isPositive().isLessThan(pagina.indexOf(">Ana<"));
    }

    @Test
    void quienNoEsMiembroNoLaVe() throws Exception {
        Liga liga = crear("Secreta");

        mvc.perform(get("/ligas/{id}", liga.getId()).with(EVA)).andExpect(status().isNotFound());
    }

    @Test
    void unCodigoQueNoExisteAvisa() throws Exception {
        mvc.perform(post("/ligas/unirse").with(EVA).with(csrf()).param("codigo", "ZZZZZZ"))
                .andExpect(redirectedUrl("/ligas"))
                .andExpect(flash().attribute("error", "No hay ninguna liga con el código ZZZZZZ"));
    }

    @Test
    void unMiembroPuedeSalirYQuienLaCreoBorrarla() throws Exception {
        Liga liga = crear("Temporal");
        mvc.perform(post("/ligas/unirse").with(LUIS).with(csrf()).param("codigo", liga.getCodigo()));

        mvc.perform(post("/ligas/{id}/borrar", liga.getId()).with(LUIS).with(csrf()))
                .andExpect(flash().attribute("error", "Solo quien ha creado la liga la puede borrar"));
        mvc.perform(post("/ligas/{id}/salir", liga.getId()).with(LUIS).with(csrf()))
                .andExpect(redirectedUrl("/ligas"));
        mvc.perform(get("/ligas/{id}", liga.getId()).with(LUIS)).andExpect(status().isNotFound());

        mvc.perform(post("/ligas/{id}/salir", liga.getId()).with(ANA).with(csrf()))
                .andExpect(flash().attribute("error", containsString("bórrala")));
        mvc.perform(post("/ligas/{id}/borrar", liga.getId()).with(ANA).with(csrf()))
                .andExpect(redirectedUrl("/ligas"));
        assertThat(ligas.findById(liga.getId())).isEmpty();
    }

    @Test
    void elCreadorDeApuestasNoJuegaEnLigas() throws Exception {
        mvc.perform(post("/ligas").with(user("creador@apuestas.es").roles("CREADOR")).with(csrf())
                        .param("nombre", "La de la casa"))
                .andExpect(flash().attribute("error", containsString("no juega")));
    }

    @Test
    void sinSesionSePideEntrar() throws Exception {
        mvc.perform(get("/ligas")).andExpect(redirectedUrl("/login"));
    }
}

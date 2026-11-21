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

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
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

    @Autowired
    private LigaService ligaService;

    @BeforeEach
    void preparar() {
        usuarioService.crear("ana@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear("luis@ucm.es", "Luis", "secreta123", Rol.USUARIO);
        usuarioService.crear("eva@ucm.es", "Eva", "secreta123", Rol.USUARIO);
        usuarios.findByEmail("luis@ucm.es").orElseThrow()
                .abonar(new BigDecimal("500"), TipoMovimiento.RECARGA, "Saldo de prueba", Hora.ahora());
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
    void laBarraIncluyeElRankingDeLigasJuntoAlRankingGeneral() throws Exception {
        String html = mvc.perform(get("/ligas/clasificacion").with(ANA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("href=\"/ligas/clasificacion\"")
                .contains("class=\"ranking-ligas")
                .contains("Ranking</span><small>Ligas</small>");
        int rankingGeneral = html.indexOf("href=\"/ranking\"");
        int rankingLigas = html.indexOf("href=\"/ligas/clasificacion\"");
        assertThat(html.indexOf("</a>", rankingGeneral)).isLessThan(rankingLigas);
    }

    @Test
    void laVistaGlobalEsPublicaYSoloMuestraResumenDeLigasPublicas() throws Exception {
        Liga privada = crear("Liga privada confidencial");
        Liga publica = crear("Liga pública visible");
        mvc.perform(post("/ligas/{id}/visibilidad", publica.getId()).with(ANA).with(csrf())
                        .param("publica", "true"))
                .andExpect(redirectedUrl("/ligas/" + publica.getId()));
        mvc.perform(post("/ligas/unirse").with(LUIS).with(csrf()).param("codigo", publica.getCodigo()))
                .andExpect(redirectedUrl("/ligas/" + publica.getId()));

        String html = mvc.perform(get("/ligas/clasificacion"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("Liga pública visible", "Global")
                .doesNotContain("Liga privada confidencial", ">Ana<", ">Luis<");
        PuestoClasificacionLiga puesto = ligaService.clasificacionGlobal().getFirst();
        assertThat(puesto.ligaId()).isEqualTo(publica.getId());
        assertThat(puesto.miembros()).isEqualTo(2);
        assertThat(puesto.saldoTotal()).isEqualByComparingTo(
                usuarios.findByEmail("ana@ucm.es").orElseThrow().getSaldo()
                        .add(usuarios.findByEmail("luis@ucm.es").orElseThrow().getSaldo()));
        String misLigas = mvc.perform(get("/ligas/clasificacion").with(ANA).param("vista", "mias"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(misLigas).contains("Liga privada confidencial", "Liga pública visible", "Visibilidad");
        assertThat(ligaService.clasificacion("ana@ucm.es")).extracting(PuestoClasificacionLiga::ligaId)
                .contains(privada.getId(), publica.getId());
    }

    @Test
    void soloElCreadorPuedeCambiarLaVisibilidadYElCambioActualizaLaClasificacion() throws Exception {
        Liga liga = crear("La liga de Ana");
        mvc.perform(post("/ligas/unirse").with(LUIS).with(csrf()).param("codigo", liga.getCodigo()));

        mvc.perform(post("/ligas/{id}/visibilidad", liga.getId()).with(LUIS).with(csrf())
                        .param("publica", "true"))
                .andExpect(redirectedUrl("/ligas/" + liga.getId()))
                .andExpect(flash().attribute("error", containsString("Solo quien ha creado")));
        assertThat(ligas.findById(liga.getId()).orElseThrow().isPublica()).isFalse();

        mvc.perform(post("/ligas/{id}/visibilidad", liga.getId()).with(ANA).with(csrf())
                        .param("publica", "true"))
                .andExpect(flash().attribute("mensaje", containsString("pública")));
        assertThat(ligas.findById(liga.getId()).orElseThrow().isPublica()).isTrue();
        assertThat(ligaService.clasificacionGlobal()).extracting(PuestoClasificacionLiga::ligaId)
                .contains(liga.getId());

        mvc.perform(post("/ligas/{id}/visibilidad", liga.getId()).with(ANA).with(csrf())
                        .param("publica", "false"))
                .andExpect(flash().attribute("mensaje", containsString("privada")));
        assertThat(ligas.findById(liga.getId()).orElseThrow().isPublica()).isFalse();
        assertThat(ligaService.clasificacionGlobal()).isEmpty();
        assertThat(ligaService.clasificacion("luis@ucm.es")).extracting(PuestoClasificacionLiga::ligaId)
                .contains(liga.getId());
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

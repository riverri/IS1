package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-10: límites que se pone el propio usuario y pausa temporal. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JuegoResponsableWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento partido;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
    }

    private Usuario ana() {
        return usuarios.findByEmail(ANA).orElseThrow();
    }

    private void apostar(String importe) {
        apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal(importe));
    }

    @Test
    void elLimiteDiarioCuentaLoYaApostado() throws Exception {
        mvc.perform(post("/cuenta/limites").with(COMO_ANA).with(csrf()).param("diario", "50").param("semanal", ""))
                .andExpect(redirectedUrl("/cuenta#juego-responsable"));
        assertThat(ana().getLimiteDiario()).isEqualByComparingTo("50");
        assertThat(ana().getLimiteSemanal()).isNull();

        apostar("30");
        assertThatThrownBy(() -> apostar("25"))
                .isInstanceOf(JuegoResponsableException.class)
                .hasMessageContaining("límite diario de 50,00")
                .hasMessageContaining("llevas 30,00");
        apostar("20");
    }

    @Test
    void lasCanceladasNoCuentan() {
        usuarioService.fijarLimites(ANA, null, new BigDecimal("40"));
        Apuesta primera = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("40"));
        apuestas.cancelar(ANA, primera.getId());

        apostar("40");
    }

    @Test
    void elDiarioNoPuedeSuperarAlSemanal() throws Exception {
        mvc.perform(post("/cuenta/limites").with(COMO_ANA).with(csrf()).param("diario", "100").param("semanal", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no puede ser mayor que el semanal")));
        assertThat(ana().getLimiteDiario()).isNull();
    }

    @Test
    void elFormularioMuestraElErrorEnLaPaginaDeApostar() throws Exception {
        usuarioService.fijarLimites(ANA, new BigDecimal("10"), null);

        mvc.perform(post("/eventos/{id}/apostar", partido.getId()).with(COMO_ANA).with(csrf())
                        .param("resultado", "LOCAL").param("importe", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Superarías tu límite diario de 10,00 monedas")));
    }

    @Test
    void conPausaNoSePuedeApostarNiSubirElImporte() throws Exception {
        Apuesta antes = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, BigDecimal.TEN);

        mvc.perform(post("/cuenta/pausa").with(COMO_ANA).with(csrf()).param("dias", "7"))
                .andExpect(flash().attribute("mensaje", containsString("Apuestas en pausa hasta el")));
        assertThat(ana().enPausa(Hora.ahora())).isTrue();

        assertThatThrownBy(() -> apostar("5")).hasMessageContaining("Has pausado tus apuestas");
        assertThatThrownBy(() -> apuestas.modificarImporte(ANA, antes.getId(), new BigDecimal("20")))
                .isInstanceOf(JuegoResponsableException.class);
        // Bajar el importe sí está permitido
        apuestas.modificarImporte(ANA, antes.getId(), new BigDecimal("5"));

        mvc.perform(get("/eventos").with(COMO_ANA))
                .andExpect(content().string(containsString("Apuestas en pausa hasta el")));
    }

    @Test
    void conPausaSePuedeConsultarLaCuenta() throws Exception {
        usuarioService.pausar(ANA, 7);

        mvc.perform(get("/cuenta").with(COMO_ANA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tienes las apuestas en pausa hasta el")))
                .andExpect(content().string(containsString("Mi cuenta")));
        mvc.perform(get("/apuestas").with(COMO_ANA))
                .andExpect(status().isOk());
    }

    @Test
    void unaPausaNoSePuedeAcortar() {
        usuarioService.pausar(ANA, 30);
        LocalDateTime hasta = ana().getPausaHasta();

        usuarioService.pausar(ANA, 1);

        assertThat(ana().getPausaHasta()).isEqualTo(hasta);
    }

    @Test
    void soloPausasDe1_7O30Dias() throws Exception {
        mvc.perform(post("/cuenta/pausa").with(COMO_ANA).with(csrf()).param("dias", "365"))
                .andExpect(flash().attribute("error", containsString("1, 7 o 30 días")));
        assertThat(ana().getPausaHasta()).isNull();
    }

    @Test
    void elCreadorPuedeDesactivarloYVolverAActivarlo() throws Exception {
        RequestPostProcessor creador = user("creador@apuestas.es").roles("CREADOR");
        usuarioService.fijarLimites(ANA, new BigDecimal("10"), null);
        usuarioService.pausar(ANA, 7);

        mvc.perform(post("/gestion/juego-responsable").with(creador).with(csrf()).param("activo", "false"))
                .andExpect(redirectedUrl("/gestion"))
                .andExpect(flash().attribute("mensaje", containsString("desactivado")));

        // Desactivado: ni pausa ni límites, y la sección desaparece de Mi cuenta
        apostar("50");
        mvc.perform(get("/cuenta").with(COMO_ANA))
                .andExpect(content().string(not(containsString("id=\"juego-responsable\""))))
                .andExpect(content().string(not(containsString("Apuestas en pausa hasta el"))));
        mvc.perform(get("/gestion").with(creador))
                .andExpect(content().string(containsString("Desactivado: no se aplican")));

        mvc.perform(post("/gestion/juego-responsable").with(creador).with(csrf()).param("activo", "true"));

        // Activado de nuevo: lo que tenía guardado vuelve a aplicarse
        assertThatThrownBy(() -> apostar("5")).hasMessageContaining("Has pausado tus apuestas");
        mvc.perform(get("/cuenta").with(COMO_ANA))
                .andExpect(content().string(containsString("id=\"juego-responsable\"")));
    }

    @Test
    void unUsuarioNormalNoPuedeDesactivarlo() throws Exception {
        mvc.perform(post("/gestion/juego-responsable").with(COMO_ANA).with(csrf()).param("activo", "false"))
                .andExpect(status().isForbidden());
    }
}

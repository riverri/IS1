package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

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
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-18: eliminar la cuenta. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EliminarCuentaWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private NotificacionService notificaciones;

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

    private Usuario ana;
    private Apuesta pendiente;

    @BeforeEach
    void preparar() {
        ana = usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        Evento jugado = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        Evento futuro = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(),
                equipos.findByNombre("Levante UD").orElseThrow(), Hora.ahora().plusDays(2)));
        apuestas.apostar(ANA, jugado.getId(), Resultado.LOCAL, BigDecimal.TEN);
        resolucion.introducirResultado(jugado.getId(), Resultado.LOCAL);
        pendiente = apuestas.apostar(ANA, futuro.getId(), Resultado.LOCAL, BigDecimal.TEN);
    }

    @Test
    void conLaContrasenaCorrectaSeAnonimizaYSeCancelanLasPendientes() throws Exception {
        Long id = ana.getId();

        mvc.perform(post("/cuenta/eliminar").with(COMO_ANA).with(csrf()).param("password", "secreta123"))
                .andExpect(redirectedUrl("/?cuentaEliminada"));

        Usuario eliminada = usuarios.findById(id).orElseThrow();
        assertThat(eliminada.isEliminado()).isTrue();
        assertThat(eliminada.getEmail()).doesNotContain("ana");
        assertThat(eliminada.getNombre()).isEqualTo("Usuario eliminado");
        assertThat(eliminada.getSaldo()).isZero();
        assertThat(pendiente.getEstado()).isEqualTo(EstadoApuesta.CANCELADA);
        assertThat(usuarios.findByEmail(ANA)).isEmpty();
        assertThat(notificaciones.sinLeer(eliminada.getEmail())).isZero();
    }

    @Test
    void desapareceDelRankingYYaNoPuedeEntrar() throws Exception {
        mvc.perform(post("/cuenta/eliminar").with(COMO_ANA).with(csrf()).param("password", "secreta123"));

        mvc.perform(get("/ranking")).andExpect(content().string(not(containsString(">Ana<"))));
        mvc.perform(formLogin("/login").userParameter("email").user(ANA).password("secreta123"))
                .andExpect(unauthenticated());
    }

    @Test
    void elEmailQuedaLibreParaOtraCuenta() throws Exception {
        mvc.perform(post("/cuenta/eliminar").with(COMO_ANA).with(csrf()).param("password", "secreta123"));

        Usuario nueva = usuarioService.crear(ANA, "Ana otra vez", "otraClave123", Rol.USUARIO);
        assertThat(nueva.getId()).isNotEqualTo(ana.getId());
    }

    @Test
    void conLaContrasenaIncorrectaNoSeElimina() throws Exception {
        mvc.perform(post("/cuenta/eliminar").with(COMO_ANA).with(csrf()).param("password", "mala"))
                .andExpect(flash().attribute("errorBaja", containsString("no es correcta")));

        assertThat(ana.isEliminado()).isFalse();
        assertThat(pendiente.getEstado()).isEqualTo(EstadoApuesta.ACTIVA);
    }

    @Test
    void elCreadorNoPuedeEliminarSuCuenta() throws Exception {
        mvc.perform(post("/cuenta/eliminar").with(user("creador@apuestas.es").roles("CREADOR")).with(csrf())
                        .param("password", "creador123"))
                .andExpect(flash().attribute("errorBaja", containsString("no se puede eliminar")));
    }
}

package es.ucm.fdi.is1.apuestas.ligas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Bloqueos de liga que se rechazan sin cobrar: a un compañero (CA-105.2, CA-105.5) y a toda la liga (CA-106.3).
 * En cada caso se comprueba que el saldo no cambia, que no queda ningún cargo de bloqueo y que no se crea el bloqueo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BloqueoLigaRechazadoTest {

    private static final String ANA = "ana-bloqueo-rechazado@ucm.es";
    private static final String LUIS = "luis-bloqueo-rechazado@ucm.es";
    private static final String EVA = "eva-bloqueo-rechazado@ucm.es";

    @Autowired private MockMvc mvc;
    @Autowired private BloqueoLigaService bloqueos;
    @Autowired private BloqueoLigaRepository bloqueoRepository;
    @Autowired private LigaService ligas;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MovimientoSaldoRepository movimientos;
    @Autowired private EventoRepository eventos;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private ResolucionService resolucion;

    private Liga liga;
    private Evento partido;
    private Long luis;
    private Long eva;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        luis = usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO).getId();
        eva = usuarioService.crear(EVA, "Eva", "secreta123", Rol.USUARIO).getId();
        liga = ligas.crear(ANA, "Amigos");
        ligas.unirse(LUIS, liga.getCodigo());
        partido = partidoEl(Hora.ahora().plusDays(1));
    }

    private Evento partidoEl(LocalDateTime fecha) {
        return eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(), equipos.findByNombre("Sevilla FC").orElseThrow(), fecha));
    }

    private BigDecimal saldo(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo();
    }

    /** Cargos por bloqueos de liga que tiene el usuario en su libro de movimientos. */
    private long cargosDeBloqueo(String email) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        return movimientos.findByUsuarioOrderByIdAsc(usuario).stream()
                .filter(m -> m.getTipo() == TipoMovimiento.BLOQUEO_LIGA).count();
    }

    /** Deja a Ana con el saldo indicado, con un ajuste que no cuenta como jugado. */
    private void dejarSaldoDeAnaEn(String importe) {
        Usuario ana = usuarios.findByEmail(ANA).orElseThrow();
        ana.ajustar(new BigDecimal(importe).subtract(ana.getSaldo()), TipoMovimiento.CORRECCION, "Saldo de prueba",
                Hora.ahora());
    }

    private void comprobarQueNoSeCobroNiSeCreo(BigDecimal saldoAntes, Evento evento) {
        assertThat(saldo(ANA)).isEqualByComparingTo(saldoAntes);
        assertThat(cargosDeBloqueo(ANA)).isZero();
        assertThat(bloqueoRepository.findByEvento(evento)).isEmpty();
    }

    // --- CA-105.2: bloqueo a un compañero ---

    @Test
    void noSePuedeBloquearAQuienNoEstaEnLaLigaNiSeCobra() {
        // Eva comparte otra liga con Ana, pero no esta: tampoco vale
        Liga otra = ligas.crear(ANA, "Otra liga");
        ligas.unirse(EVA, otra.getCodigo());
        BigDecimal antes = saldo(ANA);

        assertThatThrownBy(() -> bloqueos.crear(liga.getId(), ANA, eva, partido.getId(), "LOCAL"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("compañero de esta liga");

        comprobarQueNoSeCobroNiSeCreo(antes, partido);
    }

    @Test
    void noSePuedeBloquearAUnoMismoNiSeCobra() throws Exception {
        Long ana = usuarios.findByEmail(ANA).orElseThrow().getId();
        BigDecimal antes = saldo(ANA);

        assertThatThrownBy(() -> bloqueos.crear(liga.getId(), ANA, ana, partido.getId(), "PARTIDO"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("compañero de esta liga");
        // Tampoco con una petición directa al servidor: se avisa y no se cobra
        mvc.perform(post("/ligas/{id}/bloqueos", liga.getId()).with(user(ANA)).with(csrf())
                        .param("objetivo", ana.toString()).param("evento", partido.getId().toString())
                        .param("alcance", "PARTIDO"))
                .andExpect(redirectedUrl("/ligas/" + liga.getId()))
                .andExpect(flash().attribute("error", containsString("compañero de esta liga")))
                .andExpect(flash().attribute("mensaje", (Object) null));

        comprobarQueNoSeCobroNiSeCreo(antes, partido);
    }

    @Test
    void noSePuedeBloquearAUnCompaneroEnUnPartidoQueYaHaEmpezado() {
        Evento empezado = partidoEl(Hora.ahora().minusMinutes(10));
        BigDecimal antes = saldo(ANA);

        assertThatThrownBy(() -> bloqueos.crear(liga.getId(), ANA, luis, empezado.getId(), "LOCAL"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("futuro y programado");

        comprobarQueNoSeCobroNiSeCreo(antes, empezado);
    }

    @Test
    void noSePuedeBloquearAUnCompaneroEnUnPartidoSuspendido() {
        resolucion.suspender(partido.getId());
        BigDecimal antes = saldo(ANA);

        assertThatThrownBy(() -> bloqueos.crear(liga.getId(), ANA, luis, partido.getId(), "LOCAL"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("futuro y programado");

        comprobarQueNoSeCobroNiSeCreo(antes, partido);
    }

    @Test
    void noSePuedeBloquearAUnCompaneroEnUnPartidoAnulado() {
        resolucion.anular(partido.getId());
        BigDecimal antes = saldo(ANA);

        assertThatThrownBy(() -> bloqueos.crear(liga.getId(), ANA, luis, partido.getId(), "LOCAL"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("futuro y programado");

        comprobarQueNoSeCobroNiSeCreo(antes, partido);
    }

    // --- CA-105.5: saldo inferior a 50 monedas ---

    @Test
    void conMenosDeCincuentaMonedasNoSeAplicaElBloqueoNiCambiaElSaldo() throws Exception {
        dejarSaldoDeAnaEn("49.99");

        assertThatThrownBy(() -> bloqueos.crear(liga.getId(), ANA, luis, partido.getId(), "LOCAL"))
                .isInstanceOf(SaldoInsuficienteException.class);
        mvc.perform(post("/ligas/{id}/bloqueos", liga.getId()).with(user(ANA)).with(csrf())
                        .param("objetivo", luis.toString()).param("evento", partido.getId().toString())
                        .param("alcance", "LOCAL"))
                .andExpect(redirectedUrl("/ligas/" + liga.getId()))
                .andExpect(flash().attribute("error", "No tienes saldo suficiente para pagar el bloqueo de 50 monedas."))
                .andExpect(flash().attribute("mensaje", (Object) null));

        comprobarQueNoSeCobroNiSeCreo(new BigDecimal("49.99"), partido);
    }

    @Test
    void conCincuentaMonedasJustasSiSeAplicaElBloqueo() {
        // Límite de CA-105.5: con 50 ya alcanza, y el saldo se queda en cero
        dejarSaldoDeAnaEn("50.00");

        bloqueos.crear(liga.getId(), ANA, luis, partido.getId(), "LOCAL");

        assertThat(saldo(ANA)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(cargosDeBloqueo(ANA)).isEqualTo(1);
        assertThat(bloqueoRepository.findByEvento(partido)).hasSize(1);
    }

    // --- CA-106.3: bloqueo a toda la liga ---

    @Test
    void quienNoEsDeLaLigaNoPuedeBloquearlaEnteraNiSeLeCobra() throws Exception {
        BigDecimal antes = saldo(EVA);

        assertThatThrownBy(() -> bloqueos.crearParaLiga(liga.getId(), EVA, partido.getId(), "PARTIDO"))
                .isInstanceOf(LigaNoEncontradaException.class);
        // Por una petición directa, la liga ni siquiera existe para Eva
        mvc.perform(post("/ligas/{id}/bloqueos/todos", liga.getId()).with(user(EVA)).with(csrf())
                        .param("evento", partido.getId().toString()).param("alcance", "PARTIDO"))
                .andExpect(status().isNotFound());

        assertThat(saldo(EVA)).isEqualByComparingTo(antes);
        assertThat(cargosDeBloqueo(EVA)).isZero();
        assertThat(bloqueoRepository.findByEvento(partido)).isEmpty();
    }

    @Test
    void noSePuedeBloquearALaLigaEnUnPartidoQueYaHaEmpezado() {
        Evento empezado = partidoEl(Hora.ahora().minusMinutes(10));
        BigDecimal antes = saldo(ANA);

        assertThatThrownBy(() -> bloqueos.crearParaLiga(liga.getId(), ANA, empezado.getId(), "PARTIDO"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("futuro y programado");

        comprobarQueNoSeCobroNiSeCreo(antes, empezado);
    }

    @Test
    void noSePuedeBloquearALaLigaEnUnPartidoSuspendido() throws Exception {
        resolucion.suspender(partido.getId());
        BigDecimal antes = saldo(ANA);

        assertThatThrownBy(() -> bloqueos.crearParaLiga(liga.getId(), ANA, partido.getId(), "PARTIDO"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("futuro y programado");
        mvc.perform(post("/ligas/{id}/bloqueos/todos", liga.getId()).with(user(ANA)).with(csrf())
                        .param("evento", partido.getId().toString()).param("alcance", "PARTIDO"))
                .andExpect(redirectedUrl("/ligas/" + liga.getId()))
                .andExpect(flash().attribute("error", containsString("futuro y programado")))
                .andExpect(flash().attribute("mensaje", (Object) null));

        comprobarQueNoSeCobroNiSeCreo(antes, partido);
    }

    @Test
    void noSePuedeBloquearALaLigaEnUnPartidoAnulado() {
        resolucion.anular(partido.getId());
        BigDecimal antes = saldo(ANA);

        assertThatThrownBy(() -> bloqueos.crearParaLiga(liga.getId(), ANA, partido.getId(), "VISITANTE"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("futuro y programado");

        comprobarQueNoSeCobroNiSeCreo(antes, partido);
    }
}

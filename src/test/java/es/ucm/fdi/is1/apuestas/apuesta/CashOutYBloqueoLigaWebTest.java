package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.ligas.BloqueoLigaRepository;
import es.ucm.fdi.is1.apuestas.ligas.BloqueoLigaService;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** Pruebas de aceptación para la oferta de HU-102 y el bloqueo individual de HU-105. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CashOutYBloqueoLigaWebTest {

    private static final String ANA = "ana-cashout-bloqueo@ucm.es";
    private static final String LUIS = "luis-cashout-bloqueo@ucm.es";

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private EventoRepository eventos;
    @Autowired private ApuestaService apuestas;
    @Autowired private LigaService ligas;
    @Autowired private BloqueoLigaRepository bloqueos;
    @Autowired private BloqueoLigaService bloqueoService;
    @Autowired private ResolucionService resolucion;
    @Autowired private SupercuotaService supercuotas;
    @Autowired private es.ucm.fdi.is1.apuestas.gestion.GestionService gestion;

    private Evento futbol;
    private Evento baloncesto;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        LocalDateTime manana = Hora.ahora().plusDays(1);
        futbol = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), manana));
        baloncesto = eventos.save(new Evento(competiciones.findByNombre("Liga ACB").orElseThrow(),
                equipos.findByNombre("Unicaja").orElseThrow(),
                equipos.findByNombre("Baskonia").orElseThrow(), manana));
    }

    @Test
    void muestraOfertaDeCashOutParaLaCombinadaCompleta() throws Exception {
        apuestas.apostar(ANA, List.of(
                new SeleccionPedida(futbol.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(baloncesto.getId(), Resultado.VISITANTE, null)), new BigDecimal("20"));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/apuestas").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cobro anticipado disponible: 20,00 monedas por toda la combinada.")));
    }

    @Test
    void confirmaElCashOutUnaVezYLosResultadosPosterioresNoLoCambian() throws Exception {
        Apuesta apuesta = apuestas.apostar(ANA, List.of(
                new SeleccionPedida(futbol.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(baloncesto.getId(), Resultado.VISITANTE, null)), new BigDecimal("20"));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/apuestas/{id}/cash-out", apuesta.getId()).with(user(ANA)).with(csrf())
                        .param("ofertaVista", "20.00"))
                .andExpect(status().is3xxRedirection());

        assertThat(apuesta.getEstado()).isEqualTo(EstadoApuesta.COBRADA);
        assertThat(apuesta.getPagado()).isEqualByComparingTo("20.00");
        assertThat(apuesta.getFechaCobroAnticipado()).isNotNull();
        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo()).isEqualByComparingTo("1000.00");
        assertThat(apuestas.estadisticas(ANA).resueltas()).isZero();
        assertThat(apuestas.estadisticas(ANA).beneficio()).isEqualByComparingTo(BigDecimal.ZERO);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/apuestas/{id}/cash-out", apuesta.getId()).with(user(ANA)).with(csrf())
                        .param("ofertaVista", "20.00"))
                .andExpect(status().is3xxRedirection());
        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo()).isEqualByComparingTo("1000.00");

        resolucion.introducirResultado(futbol.getId(), Resultado.LOCAL);
        resolucion.introducirResultado(baloncesto.getId(), Resultado.LOCAL);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/apuestas").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cobro anticipado")))
                .andExpect(content().string(containsString("Abonado 20,00")));
        assertThat(apuesta.getEstado()).isEqualTo(EstadoApuesta.COBRADA);
        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo()).isEqualByComparingTo("1000.00");
    }

    @Test
    void siLaOfertaCambioNoCierraLaCombinadaNiAbonaNada() {
        Apuesta apuesta = apuestas.apostar(ANA, List.of(
                new SeleccionPedida(futbol.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(baloncesto.getId(), Resultado.VISITANTE, null)), new BigDecimal("20"));
        equipos.findByNombre("Getafe CF").orElseThrow().setCalidad(9.5);

        assertThatThrownBy(() -> apuestas.cobrarAnticipado(ANA, apuesta.getId(), new BigDecimal("20.00")))
                .isInstanceOf(OfertaCashOutCambiadaException.class);
        assertThat(apuesta.getEstado()).isEqualTo(EstadoApuesta.ACTIVA);
        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo()).isEqualByComparingTo("980.00");
    }

    @Test
    void bloqueoCuestaCincuentaYElServidorImpideApostarEnElAlcance() throws Exception {
        Liga liga = ligas.crear(ANA, "Amigos");
        ligas.unirse(LUIS, liga.getCodigo());
        BigDecimal saldoInicial = usuarios.findByEmail(ANA).orElseThrow().getSaldo();

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/ligas/{id}/bloqueos", liga.getId()).with(user(ANA)).with(csrf())
                        .param("objetivo", usuarios.findByEmail(LUIS).orElseThrow().getId().toString())
                        .param("evento", futbol.getId().toString()).param("alcance", "LOCAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensaje", containsString("50 monedas")));

        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo())
                .isEqualByComparingTo(saldoInicial.subtract(new BigDecimal("50.00")));
        assertThat(bloqueos.count()).isEqualTo(1);
        assertThatThrownBy(() -> apuestas.apostar(LUIS,
                List.of(new SeleccionPedida(futbol.getId(), Resultado.LOCAL, null)), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("bloqueada");

        // El bloqueo solo limita esa selección, no el partido completo.
        assertThat(apuestas.apostar(LUIS,
                List.of(new SeleccionPedida(futbol.getId(), Resultado.VISITANTE, null)), BigDecimal.TEN)).isNotNull();
    }

    @Test
    void bloqueoDeLigaCuestaCienYExcluyeAlCreadorDelBloqueo() throws Exception {
        Liga liga = ligas.crear(ANA, "Amigos");
        ligas.unirse(LUIS, liga.getCodigo());
        BigDecimal saldoInicial = usuarios.findByEmail(ANA).orElseThrow().getSaldo();

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/ligas/{id}/bloqueos/todos", liga.getId()).with(user(ANA)).with(csrf())
                        .param("evento", futbol.getId().toString()).param("alcance", "LOCAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensaje", containsString("100 monedas")));

        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo())
                .isEqualByComparingTo(saldoInicial.subtract(new BigDecimal("100.00")));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/ligas/{id}", liga.getId()).with(user(LUIS)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Toda la liga")));
        assertThatThrownBy(() -> apuestas.apostar(LUIS,
                List.of(new SeleccionPedida(futbol.getId(), Resultado.LOCAL, null)), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("bloqueada");
        assertThat(apuestas.apostar(ANA,
                List.of(new SeleccionPedida(futbol.getId(), Resultado.LOCAL, null)), BigDecimal.TEN)).isNotNull();
        assertThatThrownBy(() -> bloqueoService.crearParaLiga(liga.getId(), ANA, futbol.getId(), "LOCAL"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Ya hay un bloqueo");
        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo())
                .isEqualByComparingTo(saldoInicial.subtract(new BigDecimal("110.00")));
    }

    private Apuesta combinadaDeAna() {
        return apuestas.apostar(ANA, List.of(
                new SeleccionPedida(futbol.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(baloncesto.getId(), Resultado.VISITANTE, null)), new BigDecimal("20"));
    }

    private BigDecimal ofertaDe(Apuesta apuesta) {
        return apuestas.ofertasCashOut(List.of(apuesta), Hora.ahora()).get(apuesta.getId());
    }

    @Test
    void bajarLaCuotaConOtraApuestaPropiaYCancelarlaNoDaDinero() {
        Apuesta combinada = combinadaDeAna();
        Apuesta empuje1 = apuestas.apostar(ANA, futbol.getId(), Resultado.LOCAL, new BigDecimal("300"));
        Apuesta empuje2 = apuestas.apostar(ANA, baloncesto.getId(), Resultado.VISITANTE, new BigDecimal("300"));

        BigDecimal oferta = ofertaDe(combinada);
        assertThat(oferta).isEqualByComparingTo("20.00");
        apuestas.cobrarAnticipado(ANA, combinada.getId(), oferta);
        apuestas.cancelar(ANA, empuje1.getId());
        apuestas.cancelar(ANA, empuje2.getId());

        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo()).isEqualByComparingTo("1000.00");
    }

    @Test
    void inflarLaCuotaAntesDeApostarYCancelarNoDaDinero() {
        Apuesta contraria1 = apuestas.apostar(ANA, futbol.getId(), Resultado.VISITANTE, new BigDecimal("300"));
        Apuesta contraria2 = apuestas.apostar(ANA, baloncesto.getId(), Resultado.LOCAL, new BigDecimal("300"));
        Apuesta combinada = combinadaDeAna();
        apuestas.cancelar(ANA, contraria1.getId());
        apuestas.cancelar(ANA, contraria2.getId());

        BigDecimal oferta = ofertaDe(combinada);
        assertThat(oferta).isLessThanOrEqualTo(new BigDecimal("20.00"));
        apuestas.cobrarAnticipado(ANA, combinada.getId(), oferta);

        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo()).isLessThanOrEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void siOtrosJugadoresBajanLaCuotaLaOfertaSubeDeLoApostado() {
        Apuesta combinada = combinadaDeAna();
        apuestas.apostar(LUIS, futbol.getId(), Resultado.LOCAL, new BigDecimal("300"));
        apuestas.apostar(LUIS, baloncesto.getId(), Resultado.VISITANTE, new BigDecimal("300"));

        assertThat(ofertaDe(combinada)).isGreaterThan(new BigDecimal("20.00"));
    }

    @Test
    void unaSimpleTambienSePuedeCobrarAntes() throws Exception {
        // CA-75.1: «una apuesta activa, simple o combinada»
        Apuesta simple = apuestas.apostar(ANA, futbol.getId(), Resultado.LOCAL, new BigDecimal("20"));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/apuestas").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cobro anticipado disponible: 20,00 monedas.")))
                .andExpect(content().string(containsString("¿Cobrar ahora la apuesta por el importe indicado?")));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/apuestas/{id}/cash-out", simple.getId()).with(user(ANA)).with(csrf())
                        .param("ofertaVista", "20.00"))
                .andExpect(status().is3xxRedirection());

        assertThat(simple.getEstado()).isEqualTo(EstadoApuesta.COBRADA);
        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo()).isEqualByComparingTo("1000.00");
    }

    @Test
    void unaSimpleConSupercuotaNoTieneCobroAnticipado() {
        supercuotas.crear(futbol.getId(), Resultado.LOCAL, new BigDecimal("4.00"), new BigDecimal("50"), 24);
        Apuesta conSupercuota = apuestas.apostar(ANA, futbol.getId(), Resultado.LOCAL, new BigDecimal("20"));
        assertThat(conSupercuota.getSelecciones().getFirst().getCuota()).isEqualByComparingTo("4.00");
        // Con la cuota normal (2,72) valdría 4/2,72 de lo apostado nada más apostar: no se ofrece
        assertThat(ofertaDe(conSupercuota)).isNull();
        assertThatThrownBy(() -> apuestas.cobrarAnticipado(ANA, conSupercuota.getId(), new BigDecimal("20.00")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(conSupercuota.getEstado()).isEqualTo(EstadoApuesta.ACTIVA);

        // Al otro resultado del mismo partido, sin supercuota, sí
        Apuesta sinSupercuota = apuestas.apostar(ANA, futbol.getId(), Resultado.VISITANTE, new BigDecimal("20"));
        assertThat(ofertaDe(sinSupercuota)).isNotNull();
    }

    @Test
    void siUnPartidoSeSuspendeOSeAnulaNoSeOfreceCobrar() {
        // CA-75.2
        Apuesta combinada = combinadaDeAna();
        Apuesta simple = apuestas.apostar(ANA, baloncesto.getId(), Resultado.LOCAL, new BigDecimal("10"));
        assertThat(ofertaDe(combinada)).isNotNull();

        resolucion.suspender(futbol.getId());
        assertThat(ofertaDe(combinada)).isNull();
        assertThatThrownBy(() -> apuestas.cobrarAnticipado(ANA, combinada.getId(), new BigDecimal("20.00")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ofertaDe(simple)).as("la simple de otro partido sigue").isNotNull();

        resolucion.anular(baloncesto.getId());
        assertThat(ofertaDe(simple)).as("anulada: ya no está activa").isNull();
    }

    // --- #136: lo pagado por los bloqueos ---

    private BigDecimal saldoDe(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo();
    }

    @Test
    void siSeAnulaElPartidoSeDevuelvenLosBloqueos() {
        Liga liga = ligas.crear(ANA, "Amigos");
        ligas.unirse(LUIS, liga.getCodigo());
        BigDecimal antes = saldoDe(ANA);
        bloqueoService.crear(liga.getId(), ANA, usuarios.findByEmail(LUIS).orElseThrow().getId(), futbol.getId(), "LOCAL");
        bloqueoService.crearParaLiga(liga.getId(), ANA, futbol.getId(), "VISITANTE");
        assertThat(saldoDe(ANA)).isEqualByComparingTo(antes.subtract(new BigDecimal("150.00")));

        resolucion.anular(futbol.getId());

        assertThat(saldoDe(ANA)).isEqualByComparingTo(antes);
        assertThat(bloqueos.findByEvento(futbol)).isEmpty();
    }

    @Test
    void conUnResultadoNormalElBloqueoNoSeDevuelve() {
        Liga liga = ligas.crear(ANA, "Amigos");
        ligas.unirse(LUIS, liga.getCodigo());
        BigDecimal antes = saldoDe(ANA);
        bloqueoService.crear(liga.getId(), ANA, usuarios.findByEmail(LUIS).orElseThrow().getId(), futbol.getId(), "LOCAL");

        resolucion.introducirResultado(futbol.getId(), Resultado.LOCAL);

        assertThat(saldoDe(ANA)).isEqualByComparingTo(antes.subtract(new BigDecimal("50.00")));
        assertThat(bloqueos.findByEvento(futbol)).hasSize(1);
    }

    @Test
    void siSeBorraElPartidoSeDevuelveElBloqueoYSePuedeBorrar() {
        Liga liga = ligas.crear(ANA, "Amigos");
        ligas.unirse(LUIS, liga.getCodigo());
        BigDecimal antes = saldoDe(ANA);
        bloqueoService.crearParaLiga(liga.getId(), ANA, futbol.getId(), "PARTIDO");

        gestion.borrarEvento(futbol.getId());

        assertThat(saldoDe(ANA)).isEqualByComparingTo(antes);
        assertThat(eventos.findById(futbol.getId())).isEmpty();
    }

    @Test
    void conLaCuentaEnPausaNoSePuedeBloquear() {
        Liga liga = ligas.crear(ANA, "Amigos");
        ligas.unirse(LUIS, liga.getCodigo());
        BigDecimal antes = saldoDe(ANA);
        usuarioService.pausar(ANA, 1);
        Long luis = usuarios.findByEmail(LUIS).orElseThrow().getId();

        assertThatThrownBy(() -> bloqueoService.crear(liga.getId(), ANA, luis, futbol.getId(), "LOCAL"))
                .isInstanceOf(JuegoResponsableException.class);
        assertThatThrownBy(() -> bloqueoService.crearParaLiga(liga.getId(), ANA, futbol.getId(), "LOCAL"))
                .isInstanceOf(JuegoResponsableException.class);
        assertThat(saldoDe(ANA)).isEqualByComparingTo(antes);
        assertThat(bloqueos.findByEvento(futbol)).isEmpty();
    }

    @Test
    void loPagadoPorUnBloqueoCuentaComoJugadoYLoGanaLaCasa() {
        assertThat(es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento.deJuego())
                .contains(es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento.BLOQUEO_LIGA);
        assertThat(BeneficioDiarioService.DE_LA_CASA).contains(es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento.BLOQUEO_LIGA,
                es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento.DEVOLUCION);
    }
}

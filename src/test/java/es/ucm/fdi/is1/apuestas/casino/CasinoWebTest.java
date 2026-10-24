package es.ucm.fdi.is1.apuestas.casino;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.ImporteFueraDeLimitesException;
import es.ucm.fdi.is1.apuestas.apuesta.JuegoResponsableException;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldo;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * HU-56 a HU-59: el casino con la base de datos y las páginas. El azar se sustituye por un generador con semilla
 * ({@code @MockitoBean}) y, en la ruleta, se fija el número que sale.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CasinoWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");
    private static final RequestPostProcessor COMO_CREADOR = user("creador@apuestas.es").roles("CREADOR");

    @MockitoBean
    private RandomGenerator azar;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CasinoService casino;

    @Autowired
    private PartidaRepository partidas;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private MovimientoSaldoRepository movimientos;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        // Baraja y rotación de anuncios con una semilla fija; la ruleta, con el número que diga cada prueba
        SplittableRandom semilla = new SplittableRandom(3);
        when(azar.nextInt(anyInt())).thenAnswer(i -> semilla.nextInt((int) i.getArgument(0)));
    }

    private BigDecimal saldo() {
        return usuarios.findByEmail(ANA).orElseThrow().getSaldo();
    }

    private void sale(int numero) {
        when(azar.nextInt(37)).thenReturn(numero);
    }

    @Test
    void acertarElColorPagaElDobleYQuedaEnLosMovimientos() {
        // CA-56.1: el 17 es negro
        sale(17);
        BigDecimal antes = saldo();

        Partida partida = casino.girarRuleta(ANA, ApuestaRuleta.NEGRO, null, new BigDecimal("10"));

        assertThat(partida.getPremio()).isEqualByComparingTo("20");
        assertThat(partida.getResultado()).isEqualTo("Salió el 17 (negro)");
        assertThat(saldo()).isEqualByComparingTo(antes.add(BigDecimal.TEN));
        List<MovimientoSaldo> lista = movimientos.findByUsuarioOrderByIdAsc(usuarios.findByEmail(ANA).orElseThrow());
        assertThat(lista).extracting(MovimientoSaldo::getTipo)
                .endsWith(TipoMovimiento.CASINO_APUESTA, TipoMovimiento.CASINO_PREMIO);
        assertThat(lista.getLast().getSaldoResultante()).isEqualByComparingTo(saldo());
    }

    @Test
    void unPlenoPaga35a1() {
        sale(17);

        Partida partida = casino.girarRuleta(ANA, ApuestaRuleta.PLENO, 17, new BigDecimal("10"));

        assertThat(partida.getPremio()).isEqualByComparingTo("360");
    }

    @Test
    void conElCeroSePierdeElColor() {
        // CA-56.2
        sale(0);
        BigDecimal antes = saldo();

        Partida partida = casino.girarRuleta(ANA, ApuestaRuleta.ROJO, null, new BigDecimal("10"));

        assertThat(partida.getPremio()).isEqualByComparingTo("0");
        assertThat(saldo()).isEqualByComparingTo(antes.subtract(BigDecimal.TEN));
    }

    @Test
    void fueraDeLimitesSinSaldoOEnPausaNoSeJuegaNiSeCobra() {
        // CA-56.3
        sale(17);
        BigDecimal antes = saldo();
        long partidasAntes = partidas.count();

        assertThatThrownBy(() -> casino.girarRuleta(ANA, ApuestaRuleta.ROJO, null, new BigDecimal("500")))
                .isInstanceOf(ImporteFueraDeLimitesException.class);
        assertThatThrownBy(() -> casino.girarRuleta(ANA, ApuestaRuleta.PLENO, 40, new BigDecimal("10")))
                .isInstanceOf(IllegalArgumentException.class);
        usuarioService.pausar(ANA, 1);
        assertThatThrownBy(() -> casino.girarRuleta(ANA, ApuestaRuleta.ROJO, null, new BigDecimal("10")))
                .isInstanceOf(JuegoResponsableException.class);

        assertThat(saldo()).isEqualByComparingTo(antes);
        assertThat(partidas.count()).isEqualTo(partidasAntes);
    }

    @Test
    void loJugadoEnElCasinoCuentaEnLosLimitesPersonales() {
        // HU-10: límite diario de 15; la primera de 10 entra, la segunda ya no
        sale(0);
        usuarioService.fijarLimites(ANA, new BigDecimal("15"), null);

        casino.girarRuleta(ANA, ApuestaRuleta.ROJO, null, new BigDecimal("10"));

        assertThatThrownBy(() -> casino.girarRuleta(ANA, ApuestaRuleta.ROJO, null, new BigDecimal("10")))
                .isInstanceOf(JuegoResponsableException.class).hasMessageContaining("límite diario");
    }

    @Test
    void elCreadorNoJuega() {
        assertThatThrownBy(() -> casino.girarRuleta("creador@apuestas.es", ApuestaRuleta.ROJO, null, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("no puede jugar");
    }

    @Test
    void unaManoDeBlackjackCobraUnaVezYNoEnsenaLaCartaOculta() throws Exception {
        // CA-57.1 y CA-57.5: repartir dos veces sigue con la misma mano y solo se cobra una vez
        BigDecimal antes = saldo();
        Partida primera = casino.empezarBlackjack(ANA, new BigDecimal("10"));
        assertThat(primera.isEnCurso()).as("con esta semilla la mano no acaba al repartir").isTrue();
        Partida segunda = casino.empezarBlackjack(ANA, new BigDecimal("10"));

        assertThat(segunda.getId()).isEqualTo(primera.getId());
        assertThat(saldo()).isEqualByComparingTo(antes.subtract(BigDecimal.TEN));
        MesaBlackjack mesa = casino.mesa(primera);
        assertThat(mesa.cartasBanca()).hasSize(1);
        Carta oculta = primera.mano().getBanca().get(1);
        String pagina = mvc.perform(get("/casino/blackjack").param("mano", primera.getId().toString()).with(COMO_ANA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Carta oculta")))
                .andReturn().getResponse().getContentAsString();
        // La carta oculta no puede estar en la página salvo que coincida con alguna de las que sí se ven
        boolean seVeIgual = mesa.cartasJugador().contains(oculta) || mesa.cartasBanca().contains(oculta);
        if (!seVeIgual) {
            assertThat(pagina).doesNotContain(">" + oculta.getTexto() + "<");
        }
    }

    @Test
    void alPlantarseSePagaSegunElResultado() {
        // CA-57.4
        Partida partida = casino.empezarBlackjack(ANA, new BigDecimal("10"));
        BigDecimal antes = saldo();

        casino.plantarse(ANA, partida.getId());

        ResultadoMano resultado = partida.mano().resultado();
        BigDecimal esperado = switch (resultado) {
            case GANA -> new BigDecimal("20");
            case EMPATE -> new BigDecimal("10");
            case BLACKJACK -> new BigDecimal("25");
            case PIERDE -> BigDecimal.ZERO;
        };
        assertThat(partida.isEnCurso()).isFalse();
        assertThat(partida.getPremio()).isEqualByComparingTo(esperado);
        assertThat(saldo()).isEqualByComparingTo(antes.add(esperado));
    }

    @Test
    void doblarCobraOtraVezLaApuestaYTerminaLaMano() {
        // CA-57.3
        Partida partida = casino.empezarBlackjack(ANA, new BigDecimal("10"));
        BigDecimal antes = saldo();

        casino.doblar(ANA, partida.getId());

        assertThat(partida.getApostado()).isEqualByComparingTo("20");
        assertThat(partida.isEnCurso()).isFalse();
        assertThat(partida.mano().getJugador()).hasSize(3);
        assertThat(saldo()).isEqualByComparingTo(antes.subtract(BigDecimal.TEN).add(partida.getPremio()));
    }

    @Test
    void unaManoTerminadaNoSeSigueJugando() throws Exception {
        // CA-57.5: un doble clic en "pedir" después de terminar no reparte otra carta
        Partida partida = casino.empezarBlackjack(ANA, new BigDecimal("10"));
        casino.plantarse(ANA, partida.getId());

        mvc.perform(post("/casino/blackjack/" + partida.getId() + "/pedir").with(COMO_ANA).with(csrf()))
                .andExpect(redirectedUrl("/casino/blackjack?mano=" + partida.getId()))
                .andExpect(flash().attribute("error", "Esta mano ya ha terminado"));
    }

    @Test
    void laRuletaDesdeLaPaginaEnsenaElResultadoYElHistorialLoGuarda() throws Exception {
        // CA-56.1 y CA-58.1
        sale(17);
        mvc.perform(post("/casino/ruleta").with(COMO_ANA).with(csrf())
                        .param("tipo", "NEGRO").param("importe", "10"))
                .andExpect(redirectedUrl("/casino/ruleta"));
        casino.girarRuleta(ANA, ApuestaRuleta.PAR, null, new BigDecimal("5"));

        mvc.perform(get("/casino/historial").with(COMO_ANA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Salió el 17 (negro)")))
                .andExpect(content().string(containsString("Negro")))
                .andExpect(content().string(containsString("Par")));
    }

    @Test
    void elCreadorCambiaLasReglasYPuedeCerrarElCasino() throws Exception {
        // CA-59.1 y CA-59.2
        mvc.perform(post("/gestion/casino").with(COMO_CREADOR).with(csrf())
                        .param("importeMinimo", "2").param("importeMaximo", "50").param("barajas", "8")
                        .param("bancaPideCon17Blando", "true").param("pagoBlackjack", "SEIS_A_CINCO"))
                .andExpect(redirectedUrl("/gestion/casino"));

        ConfiguracionCasino configuracion = casino.configuracion();
        assertThat(configuracion.isActivo()).isFalse();
        assertThat(configuracion.getPagoBlackjack()).isEqualTo(PagoBlackjack.SEIS_A_CINCO);
        assertThat(configuracion.getVentajaBlackjack()).isEqualByComparingTo("2.70");
        assertThatThrownBy(() -> casino.girarRuleta(ANA, ApuestaRuleta.ROJO, null, BigDecimal.TEN))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("cerrado");
        mvc.perform(get("/gestion/casino").with(COMO_CREADOR))
                .andExpect(content().string(containsString("2,70 %")));
    }

    @Test
    void elMaximoNoPuedeSerMenorQueElMinimo() throws Exception {
        mvc.perform(post("/gestion/casino").with(COMO_CREADOR).with(csrf()).param("activo", "true")
                        .param("importeMinimo", "50").param("importeMaximo", "10").param("barajas", "6")
                        .param("pagoBlackjack", "TRES_A_DOS"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no puede ser menor que el mínimo")));
    }

    @Test
    void elPanelDeLaCasaIncluyeElCasino() throws Exception {
        sale(0);
        casino.girarRuleta(ANA, ApuestaRuleta.ROJO, null, new BigDecimal("10"));

        mvc.perform(get("/gestion/casa").with(COMO_CREADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ruleta europea")));
    }

    @Test
    void elCasinoExigeSesionYNoLoVeElCreador() throws Exception {
        mvc.perform(get("/casino")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/eventos").with(COMO_CREADOR))
                .andExpect(content().string(not(containsString("href=\"/casino\""))));
        mvc.perform(get("/eventos").with(COMO_ANA))
                .andExpect(content().string(containsString("href=\"/casino\"")));
    }

    @Test
    void laPortadaDelCasinoEnsenaLosJuegosYLaVentaja() throws Exception {
        mvc.perform(get("/casino").with(COMO_ANA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ruleta europea")))
                .andExpect(content().string(containsString("2,70 %")));
    }
}

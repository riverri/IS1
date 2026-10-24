package es.ucm.fdi.is1.apuestas.cripto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.ImporteFueraDeLimitesException;
import es.ucm.fdi.is1.apuestas.apuesta.JuegoResponsableException;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldo;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * HU-69 a HU-73: cartera de criptomonedas con la base de datos y las páginas. Los precios los da una
 * {@link FuentePrecios} falsa ({@code @MockitoBean}): las pruebas no salen a Internet. Las criptos son las de la
 * migración V15.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CriptoWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");
    private static final RequestPostProcessor COMO_CREADOR = user("creador@apuestas.es").roles("CREADOR");
    private static final BigDecimal VEINTE_CENTIMOS = new BigDecimal("0.20");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CriptoService cripto;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private MovimientoSaldoRepository movimientos;

    @Autowired
    private PrecioCriptoRepository historico;

    @MockitoBean
    private FuentePrecios fuente;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        precios("0.20", "0.0000123456789");
    }

    /** DOGE, PEPE y BTC con precio; el resto (SOL…) sin precio, como si CoinGecko no los diera. */
    private void precios(String doge, String pepe) {
        given(fuente.precios(any())).willReturn(Map.of(
                "dogecoin", new FuentePrecios.Precio(new BigDecimal(doge), new BigDecimal("5.5")),
                "pepe", new FuentePrecios.Precio(new BigDecimal(pepe), new BigDecimal("-31.2")),
                "bitcoin", new FuentePrecios.Precio(new BigDecimal("84312.55"), null)));
        cripto.actualizarPrecios();
    }

    private BigDecimal saldo() {
        return usuarios.findByEmail(ANA).orElseThrow().getSaldo();
    }

    @Test
    void laListaEsPublicaConLasMemecoinsPrimeroYElAvisoDeRiesgo() throws Exception {
        // CA-69.1 y CA-69.3
        String pagina = mvc.perform(get("/cripto"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Precios reales, dinero de mentira")))
                .andExpect(content().string(containsString("Riesgo muy alto")))
                // PEPE es la que más se ha movido: -31,2 %
                .andExpect(content().string(containsString("La que más cae hoy")))
                .andExpect(content().string(containsString("0,00001235 €")))
                .andExpect(content().string(containsString("84.312,55 €")))
                .andReturn().getResponse().getContentAsString();
        assertThat(pagina.indexOf("FARTCOIN")).isLessThan(pagina.indexOf("BTC"));
    }

    @Test
    void actualizarGuardaElPrecioYLaVariacionDeCadaCripto() {
        // CA-69.4
        Criptomoneda doge = cripto.cripto("doge");
        assertThat(doge.getPrecio()).isEqualByComparingTo("0.20");
        assertThat(doge.getVariacion24h()).isEqualByComparingTo("5.5");
        assertThat(cripto.precioAlDia(doge)).isTrue();
        assertThat(cripto.cripto("PEPE").getPrecio()).isEqualByComparingTo("0.000012345679");
        assertThat(cripto.precioAlDia(cripto.cripto("SOL"))).isFalse();
    }

    @Test
    void laFichaDibujaElGraficoDeLaSemana() throws Exception {
        // CA-69.2: con un solo precio no hay línea; con uno de hace dos horas, sí
        mvc.perform(get("/cripto/DOGE"))
                .andExpect(content().string(containsString("Aún no hay suficientes precios")));
        historico.save(new PrecioCripto(cripto.cripto("DOGE"), new BigDecimal("0.18"), Hora.ahora().minusHours(2)));

        mvc.perform(get("/cripto/DOGE"))
                .andExpect(content().string(containsString("Precio en los últimos 7 días")))
                .andExpect(content().string(containsString("Mínimo 0,18 €")));
    }

    @Test
    void laFichaEsPublicaYEnsenaLaPrevisionDeCompra() throws Exception {
        // CA-70.1: 100 monedas con un 1 % de comisión → 99 / 0,20 = 495 DOGE
        mvc.perform(get("/cripto/DOGE").param("importe", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dogecoin (DOGE)")))
                .andExpect(content().string(containsString("Recibirías 495 DOGE")));
        mvc.perform(get("/cripto/NOEXISTE")).andExpect(status().isNotFound());
    }

    @Test
    void comprarCobraElImporteConComisionYApuntaElMovimiento() {
        // CA-70.1 y HU-55
        BigDecimal antes = saldo();

        PosicionCripto posicion = cripto.comprar(ANA, "DOGE", new BigDecimal("100"), VEINTE_CENTIMOS);

        assertThat(saldo()).isEqualByComparingTo(antes.subtract(new BigDecimal("100")));
        assertThat(posicion.getCantidad()).isEqualByComparingTo("495");
        assertThat(posicion.getCoste()).isEqualByComparingTo("100");
        List<MovimientoSaldo> mios = movimientos.findByUsuarioOrderByIdAsc(usuarios.findByEmail(ANA).orElseThrow());
        assertThat(mios.getLast().getTipo()).isEqualTo(TipoMovimiento.CRIPTO_COMPRA);
        assertThat(mios.getLast().getImporte()).isEqualByComparingTo("-100");
    }

    @Test
    void conMemecoinsDeFraccionesDeCentimoSeCompranMillonesDeUnidades() {
        // 10 monedas de PEPE a 0,000012345679 €: más de 800.000 unidades, redondeando hacia abajo
        PosicionCripto posicion = cripto.comprar(ANA, "PEPE", new BigDecimal("10"), null);
        assertThat(posicion.getCantidad()).isGreaterThan(new BigDecimal("800000"));
        assertThat(posicion.getValor()).isLessThanOrEqualTo(new BigDecimal("9.90"));
    }

    @Test
    void venderDevuelveElValorMenosLaComisionYBajaElCosteEnProporcion() {
        // CA-71.1: compra a 0,20 y la memecoin dobla su precio
        cripto.comprar(ANA, "DOGE", new BigDecimal("100"), null);
        precios("0.40", "0.0000123456789");
        BigDecimal antes = saldo();

        // Vende la mitad: 247,5 × 0,40 = 99,00 − 0,99 de comisión
        BigDecimal recibido = cripto.vender(ANA, "DOGE", new BigDecimal("247.5"), new BigDecimal("0.40"));

        assertThat(recibido).isEqualByComparingTo("98.01");
        assertThat(saldo()).isEqualByComparingTo(antes.add(new BigDecimal("98.01")));
        PosicionCripto posicion = cripto.posicion(ANA, "DOGE");
        assertThat(posicion.getCantidad()).isEqualByComparingTo("247.5");
        assertThat(posicion.getCoste()).isEqualByComparingTo("50.00");
        assertThat(posicion.getGanancia()).isEqualByComparingTo("49.00");

        // Vender todo deja la cartera vacía
        cripto.vender(ANA, "DOGE", null, null);
        assertThat(cripto.posicion(ANA, "DOGE")).isNull();
        assertThat(cripto.cartera(ANA).posiciones()).isEmpty();
    }

    @Test
    void noSeVendeMasDeLoQueSeTieneNiLoQueNoSeTiene() {
        // CA-71.2
        cripto.comprar(ANA, "DOGE", new BigDecimal("10"), null);
        BigDecimal antes = saldo();

        assertThatThrownBy(() -> cripto.vender(ANA, "DOGE", new BigDecimal("1000"), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("No tienes tanto");
        assertThatThrownBy(() -> cripto.vender(ANA, "BTC", new BigDecimal("1"), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("No tienes BTC");
        assertThat(saldo()).isEqualByComparingTo(antes);
    }

    @Test
    void sinSaldoFueraDeLimitesOEnPausaNoSeCompraNiSeCobra() {
        // CA-70.2
        BigDecimal antes = saldo();
        assertThatThrownBy(() -> cripto.comprar(ANA, "DOGE", new BigDecimal("501"), null))
                .isInstanceOf(ImporteFueraDeLimitesException.class);
        assertThatThrownBy(() -> cripto.comprar(ANA, "DOGE", new BigDecimal("1.234"), null))
                .isInstanceOf(IllegalArgumentException.class);
        for (int i = 0; i < 2; i++) {
            cripto.comprar(ANA, "BTC", new BigDecimal("500"), null);
        }
        assertThatThrownBy(() -> cripto.comprar(ANA, "DOGE", new BigDecimal("10"), null))
                .isInstanceOf(SaldoInsuficienteException.class);
        usuarioService.pausar(ANA, 1);
        assertThatThrownBy(() -> cripto.comprar(ANA, "DOGE", new BigDecimal("1"), null))
                .isInstanceOf(JuegoResponsableException.class);
        assertThat(saldo()).isEqualByComparingTo(antes.subtract(new BigDecimal("1000")));
    }

    @Test
    void loCompradoCuentaEnLosLimitesPersonales() {
        // HU-10: límite diario de 15
        usuarioService.fijarLimites(ANA, new BigDecimal("15"), null);
        cripto.comprar(ANA, "DOGE", new BigDecimal("10"), null);

        assertThatThrownBy(() -> cripto.comprar(ANA, "DOGE", new BigDecimal("10"), null))
                .isInstanceOf(JuegoResponsableException.class).hasMessageContaining("límite diario");
    }

    @Test
    void siElPrecioHaCambiadoOnoEstaAlDiaNoSeCompra() {
        // CA-70.3 y CA-70.4
        BigDecimal antes = saldo();
        assertThatThrownBy(() -> cripto.comprar(ANA, "DOGE", new BigDecimal("10"), new BigDecimal("0.19")))
                .isInstanceOf(PrecioCambiadoException.class);
        assertThatThrownBy(() -> cripto.comprar(ANA, "SOL", new BigDecimal("10"), null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("precio al día");
        assertThat(saldo()).isEqualByComparingTo(antes);
    }

    @Test
    void elCreadorNoCompraYConLaSeccionCerradaNadieCompra() {
        // CA-70.2 y CA-73.1
        assertThatThrownBy(() -> cripto.comprar("creador@apuestas.es", "DOGE", new BigDecimal("10"), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("creador");
        cripto.configurar(false, new BigDecimal("1"), 15);
        assertThatThrownBy(() -> cripto.comprar(ANA, "DOGE", new BigDecimal("10"), null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("cerrada");
    }

    @Test
    void unaCriptoRetiradaNoSeCompraPeroQuienLaTieneLaPuedeVender() {
        // CA-73.2
        cripto.comprar(ANA, "DOGE", new BigDecimal("10"), null);
        cripto.activar("DOGE", false);

        assertThat(cripto.criptos()).extracting(Criptomoneda::getSimbolo).doesNotContain("DOGE");
        assertThatThrownBy(() -> cripto.comprar(ANA, "DOGE", new BigDecimal("10"), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cripto.vender(ANA, "DOGE", null, null)).isPositive();
    }

    @Test
    void comprarDesdeLaWebYVerLaCartera() throws Exception {
        // CA-70.1 y CA-72.1
        mvc.perform(post("/cripto/DOGE/comprar").with(COMO_ANA).with(csrf())
                        .param("importe", "100").param("precioVisto", "0.200000000000"))
                .andExpect(redirectedUrl("/cripto/DOGE"))
                .andExpect(flash().attribute("mensaje", containsString("Ahora tienes 495 DOGE")));

        mvc.perform(get("/cripto/cartera").with(COMO_ANA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("DOGE")))
                .andExpect(content().string(containsString("Compra de DOGE")));
    }

    @Test
    void conElPrecioCambiadoLaWebVuelveALaPrevision() throws Exception {
        // CA-70.3
        mvc.perform(post("/cripto/DOGE/comprar").with(COMO_ANA).with(csrf())
                        .param("importe", "100").param("precioVisto", "0.19"))
                .andExpect(redirectedUrl("/cripto/DOGE?importe=100"))
                .andExpect(flash().attribute("aviso", containsString("ha cambiado")));
    }

    @Test
    void laCarteraYComprarExigenSesion() throws Exception {
        mvc.perform(get("/cripto/cartera")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/cripto/DOGE/comprar").with(csrf()).param("importe", "10"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void elCreadorConfiguraRetiraYActualizaDesdeGestion() throws Exception {
        // CA-73.1 a CA-73.3
        mvc.perform(get("/gestion/cripto").with(COMO_CREADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FARTCOIN")));
        mvc.perform(post("/gestion/cripto").with(COMO_CREADOR).with(csrf())
                        .param("activo", "true").param("comision", "2.5").param("minutosValidez", "10"))
                .andExpect(flash().attribute("mensaje", "Configuración guardada"));
        assertThat(cripto.configuracion().getComision()).isEqualByComparingTo("0.025");
        assertThat(cripto.configuracion().getMinutosValidez()).isEqualTo(10);

        mvc.perform(post("/gestion/cripto").with(COMO_CREADOR).with(csrf())
                        .param("activo", "true").param("comision", "50").param("minutosValidez", "10"))
                .andExpect(flash().attribute("error", containsString("comisión")));

        mvc.perform(post("/gestion/cripto/WIF/activar").with(COMO_CREADOR).with(csrf()).param("activa", "false"))
                .andExpect(redirectedUrl("/gestion/cripto"));
        assertThat(cripto.cripto("WIF").isActiva()).isFalse();

        mvc.perform(post("/gestion/cripto/actualizar").with(COMO_CREADOR).with(csrf()))
                .andExpect(flash().attribute("mensaje", containsString("3 criptomonedas")));
        given(fuente.precios(any())).willThrow(new ResourceAccessException("sin conexión"));
        mvc.perform(post("/gestion/cripto/actualizar").with(COMO_CREADOR).with(csrf()))
                .andExpect(flash().attribute("error", containsString("CoinGecko")));
    }

    @Test
    void soloElCreadorEntraEnLaGestionYVeLasCuentasEnElPanelDeLaCasa() throws Exception {
        cripto.comprar(ANA, "DOGE", new BigDecimal("100"), null);
        mvc.perform(get("/gestion/cripto").with(COMO_ANA)).andExpect(status().isForbidden());

        CriptoService.CuentasCripto cuentas = cripto.cuentas();
        assertThat(cuentas.compras()).isEqualByComparingTo("100");
        assertThat(cuentas.comisiones()).isEqualByComparingTo("1");
        assertThat(cuentas.carteras()).isEqualByComparingTo("99");
        assertThat(cuentas.getBeneficio()).isEqualByComparingTo("1");
        mvc.perform(get("/gestion/casa").with(COMO_CREADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Valor de las carteras")));
    }
}

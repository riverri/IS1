package es.ucm.fdi.is1.apuestas.prediccion;

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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.ImporteFueraDeLimitesException;
import es.ucm.fdi.is1.apuestas.apuesta.JuegoResponsableException;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaService;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldo;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-63 a HU-68: mercados de predicción con la base de datos y las páginas. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrediccionWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final String LUIS = "luis@ucm.es";
    private static final String EVA = "eva@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");
    private static final RequestPostProcessor COMO_CREADOR = user("creador@apuestas.es").roles("CREADOR");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PrediccionService predicciones;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private MovimientoSaldoRepository movimientos;

    @Autowired
    private LigaService ligas;

    @Autowired
    private NotificacionService notificaciones;

    private MercadoPrediccion mercado;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        usuarioService.crear(EVA, "Eva", "secreta123", Rol.USUARIO);
        mercado = predicciones.crear("¿Lloverá en la final?", null, Hora.ahora().plusDays(5), new BigDecimal("100"),
                new BigDecimal("2"));
    }

    private BigDecimal saldo(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo();
    }

    private Long id() {
        return mercado.getId();
    }

    @Test
    void unMercadoNuevoEmpiezaAlCincuentaPorCiento() {
        // CA-63.1
        assertThat(mercado.getEstado()).isEqualTo(EstadoPrediccion.ABIERTO);
        assertThat(mercado.getPrecioSi()).isEqualByComparingTo("0.5");
    }

    @Test
    void comprarCobraElImporteDaParticipacionesYSubeElPrecio() {
        // CA-64.1
        BigDecimal antes = saldo(ANA);
        PrediccionService.Cotizacion cotizacion = predicciones.cotizarCompra(id(), Lado.SI, new BigDecimal("20"));

        PosicionPrediccion posicion = predicciones.comprar(ANA, id(), Lado.SI, new BigDecimal("20"),
                mercado.getPrecioSi());

        assertThat(saldo(ANA)).isEqualByComparingTo(antes.subtract(new BigDecimal("20")));
        assertThat(posicion.getParticipaciones()).isEqualByComparingTo(cotizacion.participaciones());
        // Quedan 19,60 tras el 2 % de comisión. A 0,50 darían 39,2 participaciones, pero el precio sube mientras se
        // compra, así que salen menos; y siempre más de 19,60, porque cada una cuesta menos de 1 monedita
        assertThat(posicion.getParticipaciones()).isBetween(new BigDecimal("19.6"), new BigDecimal("39.2"));
        assertThat(mercado.getPrecioSi()).isGreaterThan(new BigDecimal("0.5"));
        assertThat(cotizacion.precioSiDespues()).isEqualByComparingTo(mercado.getPrecioSi());
        MovimientoSaldo ultimo = movimientos.findByUsuarioOrderByIdAsc(usuarios.findByEmail(ANA).orElseThrow()).getLast();
        assertThat(ultimo.getTipo()).isEqualTo(TipoMovimiento.PREDICCION_COMPRA);
    }

    @Test
    void noSeCompraSinSaldoFueraDeLimitesNiEnUnMercadoCerrado() {
        // CA-64.2
        assertThatThrownBy(() -> predicciones.comprar(ANA, id(), Lado.SI, new BigDecimal("600"), null))
                .isInstanceOf(ImporteFueraDeLimitesException.class);
        usuarios.findByEmail(ANA).orElseThrow().cargar(new BigDecimal("995"), TipoMovimiento.APUESTA, "Gasto", Hora.ahora());
        assertThatThrownBy(() -> predicciones.comprar(ANA, id(), Lado.SI, new BigDecimal("10"), null))
                .isInstanceOf(SaldoInsuficienteException.class);
        assertThatThrownBy(() -> predicciones.comprar("creador@apuestas.es", id(), Lado.SI, BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("creador");
        predicciones.cerrar(id());
        assertThatThrownBy(() -> predicciones.comprar(LUIS, id(), Lado.SI, new BigDecimal("10"), null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("ya no admite");
    }

    @Test
    void elJuegoResponsableTambienCuenta() {
        // CA-64.2 y HU-10
        usuarioService.pausar(ANA, 1);

        assertThatThrownBy(() -> predicciones.comprar(ANA, id(), Lado.NO, BigDecimal.TEN, null))
                .isInstanceOf(JuegoResponsableException.class);
    }

    @Test
    void siElPrecioCambiaSePideConfirmarOtraVez() throws Exception {
        // CA-64.3: Ana ve 0,50; Luis compra antes que ella
        BigDecimal visto = mercado.getPrecioSi();
        predicciones.comprar(LUIS, id(), Lado.SI, new BigDecimal("30"), null);
        BigDecimal antes = saldo(ANA);

        mvc.perform(post("/predicciones/" + id() + "/comprar").with(COMO_ANA).with(csrf())
                        .param("lado", "SI").param("importe", "10").param("precioVisto", visto.toPlainString()))
                .andExpect(redirectedUrl("/predicciones/" + id() + "?lado=SI&importe=10"))
                .andExpect(flash().attributeExists("aviso"));

        assertThat(saldo(ANA)).isEqualByComparingTo(antes);
    }

    @Test
    void venderDevuelveSuValorMenosLaComisionYBajaElPrecio() {
        // CA-65.1
        PosicionPrediccion posicion = predicciones.comprar(ANA, id(), Lado.SI, new BigDecimal("20"), null);
        BigDecimal precioAntes = mercado.getPrecioSi();
        BigDecimal saldoAntes = saldo(ANA);
        BigDecimal mitad = posicion.getParticipaciones().divide(BigDecimal.valueOf(2));

        BigDecimal recibido = predicciones.vender(ANA, id(), Lado.SI, mitad, null);

        assertThat(recibido).isPositive().isLessThan(new BigDecimal("10"));
        assertThat(saldo(ANA)).isEqualByComparingTo(saldoAntes.add(recibido));
        assertThat(mercado.getPrecioSi()).isLessThan(precioAntes);
    }

    @Test
    void noSePuedeVenderMasDeLoQueSeTiene() {
        // CA-65.2
        PosicionPrediccion posicion = predicciones.comprar(ANA, id(), Lado.SI, new BigDecimal("10"), null);

        assertThatThrownBy(() -> predicciones.vender(ANA, id(), Lado.SI, posicion.getParticipaciones().add(BigDecimal.ONE), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("No tienes tantas");
        assertThatThrownBy(() -> predicciones.vender(LUIS, id(), Lado.SI, BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void alResolverseCadaParticipacionGanadoraPagaUnaMonedita() {
        // CA-66.1
        PosicionPrediccion deAna = predicciones.comprar(ANA, id(), Lado.SI, new BigDecimal("20"), null);
        predicciones.comprar(LUIS, id(), Lado.NO, new BigDecimal("20"), null);
        BigDecimal anaAntes = saldo(ANA);
        BigDecimal luisAntes = saldo(LUIS);

        predicciones.resolver(id(), Lado.SI);

        BigDecimal cobrado = deAna.getParticipaciones().setScale(2, java.math.RoundingMode.DOWN);
        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes.add(cobrado));
        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes);
        assertThat(notificaciones.leer(ANA)).anyMatch(a -> a.texto().contains("Has acertado"));
        assertThat(notificaciones.leer(LUIS)).anyMatch(a -> a.texto().contains("ha sido «Sí»"));
    }

    @Test
    void corregirLaRespuestaAjustaLosSaldosPorLaDiferencia() {
        // CA-66.2
        PosicionPrediccion deAna = predicciones.comprar(ANA, id(), Lado.SI, new BigDecimal("20"), null);
        PosicionPrediccion deLuis = predicciones.comprar(LUIS, id(), Lado.NO, new BigDecimal("20"), null);
        BigDecimal anaAntes = saldo(ANA);
        BigDecimal luisAntes = saldo(LUIS);

        predicciones.resolver(id(), Lado.SI);
        predicciones.resolver(id(), Lado.NO);

        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes);
        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes.add(deLuis.getParticipaciones()
                .setScale(2, java.math.RoundingMode.DOWN)));
        assertThat(deAna.getPagado()).isEqualByComparingTo("0");
        assertThat(movimientos.findByUsuarioOrderByIdAsc(usuarios.findByEmail(ANA).orElseThrow()).getLast().getTipo())
                .isEqualTo(TipoMovimiento.CORRECCION);
    }

    @Test
    void alAnularSeDevuelveLoPuestoMenosLoRecuperado() {
        // CA-63.3
        BigDecimal antes = saldo(ANA);
        PosicionPrediccion posicion = predicciones.comprar(ANA, id(), Lado.SI, new BigDecimal("20"), null);
        predicciones.vender(ANA, id(), Lado.SI, posicion.getParticipaciones().divide(BigDecimal.valueOf(4)), null);

        predicciones.anular(id());

        assertThat(saldo(ANA)).isEqualByComparingTo(antes);
        assertThat(mercado.getEstado()).isEqualTo(EstadoPrediccion.ANULADO);
        assertThatThrownBy(() -> predicciones.resolver(id(), Lado.SI)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unVisitanteVeElPrecioLasEstadisticasYElGrafico() throws Exception {
        // HU-67, CA-67.1
        predicciones.comprar(ANA, id(), Lado.SI, new BigDecimal("20"), null);
        predicciones.comprar(LUIS, id(), Lado.NO, new BigDecimal("5"), null);

        mvc.perform(get("/predicciones"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("¿Lloverá en la final?")));
        mvc.perform(get("/predicciones/" + id()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("participantes")))
                .andExpect(content().string(containsString("Evolución del precio")))
                .andExpect(content().string(containsString("<path")));
        assertThat(predicciones.estadisticas(id()).participantes()).isEqualTo(2);
        assertThat(predicciones.estadisticas(id()).volumen()).isEqualByComparingTo("25");
    }

    @Test
    void laPrevisionEnsenaCuantasParticipacionesSeRecibirian() throws Exception {
        // CA-64.1
        mvc.perform(get("/predicciones/" + id()).param("lado", "SI").param("importe", "10").with(COMO_ANA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Recibirías")))
                .andExpect(content().string(containsString("Confirmar la compra")));
    }

    @Test
    void veoLasPosicionesDeMisCompanerosDeLigaYNoLasDeLosDemas() throws Exception {
        // CA-68.1 y CA-68.2: Ana y Luis comparten liga; Eva no
        Liga liga = ligas.crear(ANA, "Amigos");
        ligas.unirse(LUIS, liga.getCodigo());
        predicciones.comprar(LUIS, id(), Lado.NO, new BigDecimal("15"), null);
        predicciones.comprar(EVA, id(), Lado.SI, new BigDecimal("15"), null);

        List<PosicionPrediccion> deMisLigas = predicciones.deMisLigas(ANA, id());

        assertThat(deMisLigas).extracting(p -> p.getUsuario().getEmail()).containsExactly(LUIS);
        assertThat(predicciones.deMisLigas(EVA, id())).isEmpty();
        mvc.perform(get("/predicciones/" + id()).with(COMO_ANA))
                .andExpect(content().string(containsString("Tus compañeros de liga")))
                .andExpect(content().string(containsString("Luis")))
                .andExpect(content().string(not(containsString("<td>Eva</td>"))));
    }

    @Test
    void elCreadorCreaCierraYResuelveDesdeGestion() throws Exception {
        mvc.perform(post("/gestion/predicciones").with(COMO_CREADOR).with(csrf())
                        .param("pregunta", "¿Habrá prórroga?").param("cierre", "2099-01-01T12:00")
                        .param("liquidez", "50").param("comision", "1"))
                .andExpect(redirectedUrl("/gestion/predicciones"))
                .andExpect(flash().attribute("mensaje", "Mercado creado: ¿Habrá prórroga?"));
        mvc.perform(post("/gestion/predicciones/" + id() + "/cerrar").with(COMO_CREADOR).with(csrf()))
                .andExpect(redirectedUrl("/gestion/predicciones"));
        mvc.perform(post("/gestion/predicciones/" + id() + "/resolver").with(COMO_CREADOR).with(csrf())
                        .param("respuesta", "SI"))
                .andExpect(redirectedUrl("/gestion/predicciones"));

        assertThat(mercado.getEstado()).isEqualTo(EstadoPrediccion.RESUELTO);
        mvc.perform(get("/gestion/predicciones").with(COMO_CREADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("¿Habrá prórroga?")));
        mvc.perform(get("/gestion/casa").with(COMO_CREADOR))
                .andExpect(content().string(containsString("Mercados de predicción")));
    }

    @Test
    void comprarExigeSesion() throws Exception {
        mvc.perform(post("/predicciones/" + id() + "/comprar").with(csrf()).param("lado", "SI").param("importe", "5"))
                .andExpect(redirectedUrl("/login"));
    }
}

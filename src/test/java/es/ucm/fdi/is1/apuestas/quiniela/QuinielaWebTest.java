package es.ucm.fdi.is1.apuestas.quiniela;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.gestion.GestionService;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldo;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-78 a HU-80: montar la quiniela, echar un boleto y repartir el bote. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuinielaWebTest {

    private static final RequestPostProcessor CREADOR = user("creador@apuestas.es").roles("CREADOR");
    private static final String ANA = "ana-quiniela@ucm.es";
    private static final String BEA = "bea-quiniela@ucm.es";
    private static final String CARLA = "carla-quiniela@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private QuinielaService quinielas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private GestionService gestion;

    @Autowired
    private NotificacionService notificaciones;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private MovimientoSaldoRepository movimientos;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento primero;
    private Evento segundo;
    private Evento tercero;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(BEA, "Bea", "secreta123", Rol.USUARIO);
        usuarioService.crear(CARLA, "Carla", "secreta123", Rol.USUARIO);
        LocalDateTime manana = Hora.ahora().plusDays(1);
        // Se guardan desordenados: la quiniela los pone en orden de fecha
        tercero = partido("Real Madrid", "Elche CF", manana.plusHours(4));
        primero = partido("Getafe CF", "Sevilla FC", manana);
        segundo = partido("Elche CF", "Levante UD", manana.plusHours(2));
    }

    private Evento partido(String local, String visitante, LocalDateTime fecha) {
        return eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre(local).orElseThrow(), equipos.findByNombre(visitante).orElseThrow(), fecha));
    }

    private Quiniela crear() {
        return quinielas.crear("Jornada 14", new BigDecimal("10"), List.of(tercero.getId(), primero.getId(), segundo.getId()));
    }

    @Autowired
    private jakarta.persistence.EntityManager em;

    /** La versión de la quiniela guardada en la base de datos (el bloqueo optimista). */
    private long version(Quiniela quiniela) {
        em.flush();
        return ((Number) em.createNativeQuery("select version from quiniela where id = ?1")
                .setParameter(1, quiniela.getId()).getSingleResult()).longValue();
    }

    private BigDecimal saldo(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo();
    }

    private List<MovimientoSaldo> movimientosDe(String email, TipoMovimiento tipo) {
        return movimientos.findByUsuarioOrderByIdAsc(usuarios.findByEmail(email).orElseThrow()).stream()
                .filter(m -> m.getTipo() == tipo).toList();
    }

    // --- HU-78 ---

    @Test
    void elCreadorMontaLaQuinielaConLosPartidosEnOrdenDeFecha() throws Exception {
        mvc.perform(post("/gestion/quinielas").with(CREADOR).with(csrf())
                        .param("nombre", "Jornada 14").param("precio", "2.50")
                        .param("eventos", tercero.getId().toString(), primero.getId().toString(), segundo.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensaje", "Quiniela «Jornada 14» creada con 3 partidos."));

        Quiniela quiniela = quinielas.todas().get(0).quiniela();
        assertThat(quiniela.getPrecio()).isEqualByComparingTo("2.50");
        assertThat(quiniela.getComision()).isEqualByComparingTo("0.10");
        assertThat(quiniela.getPartidos()).extracting(PartidoQuiniela::getEvento).containsExactly(primero, segundo, tercero);
        mvc.perform(get("/gestion/quinielas").with(CREADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Jornada 14")));
    }

    @Test
    void conMenosDeTresPartidosNoSeCrea() throws Exception {
        mvc.perform(post("/gestion/quinielas").with(CREADOR).with(csrf())
                        .param("nombre", "Corta").param("precio", "2")
                        .param("eventos", primero.getId().toString(), segundo.getId().toString()))
                .andExpect(flash().attribute("error", "Una quiniela lleva de 3 a 15 partidos; has elegido 2"));
        assertThat(quinielas.todas()).noneMatch(r -> r.quiniela().getNombre().equals("Corta"));
    }

    @Test
    void crearValidaNombrePrecioDuplicadosYPartidosInexistentes() {
        assertThatThrownBy(() -> quinielas.crear("   ", BigDecimal.ONE,
                List.of(primero.getId(), segundo.getId(), tercero.getId())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Ponle un nombre");
        assertThatThrownBy(() -> quinielas.crear("Sin precio", BigDecimal.ZERO,
                List.of(primero.getId(), segundo.getId(), tercero.getId())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("precio del boleto");
        assertThatThrownBy(() -> quinielas.crear("Demasiados decimales", new BigDecimal("1.001"),
                List.of(primero.getId(), segundo.getId(), tercero.getId())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("2 decimales");
        Quiniela sinDuplicados = quinielas.crear("Duplicado", BigDecimal.ONE,
                List.of(primero.getId(), segundo.getId(), tercero.getId(), tercero.getId()));
        assertThat(sinDuplicados.getPartidos()).extracting(PartidoQuiniela::getEvento)
                .containsExactly(primero, segundo, tercero);
        assertThatThrownBy(() -> quinielas.crear("Desaparecido", BigDecimal.ONE,
                List.of(primero.getId(), segundo.getId(), Long.MAX_VALUE)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Alguno de los partidos ya no existe");
    }

    @Test
    void noValenPartidosDeDeportesSinEmpateNiYaEmpezados() {
        Evento basket = eventos.save(new Evento(competiciones.findByNombre("Liga ACB").orElseThrow(),
                equipos.findByNombre("Unicaja").orElseThrow(), equipos.findByNombre("Baskonia").orElseThrow(),
                Hora.ahora().plusDays(1)));
        assertThatThrownBy(() -> quinielas.crear("Con basket", BigDecimal.ONE,
                List.of(primero.getId(), segundo.getId(), basket.getId())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("no hay empate");
        assertThat(quinielas.partidosDisponibles()).contains(primero).doesNotContain(basket);

        Evento empezado = partido("Getafe CF", "Levante UD", Hora.ahora().minusMinutes(5));
        assertThatThrownBy(() -> quinielas.crear("Tarde", BigDecimal.ONE,
                List.of(primero.getId(), segundo.getId(), empezado.getId())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ya ha empezado");
    }

    @Test
    void unUsuarioNoEntraEnLaGestionDeQuinielas() throws Exception {
        mvc.perform(get("/gestion/quinielas").with(user(ANA).roles("USUARIO"))).andExpect(status().isForbidden());
        mvc.perform(post("/gestion/quinielas").with(user(ANA).roles("USUARIO")).with(csrf())
                .param("nombre", "Mía").param("precio", "1")).andExpect(status().isForbidden());
    }

    @Test
    void unEventoDeUnaQuinielaNoSeBorra() {
        crear();
        assertThatThrownBy(() -> gestion.borrarEvento(primero.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("está en una quiniela");
        assertThat(eventos.existsById(primero.getId())).isTrue();
    }

    // --- HU-79 ---

    @Test
    void echoMiBoletoYSeCobraElPrecioUnaSolaVez() throws Exception {
        Quiniela quiniela = crear();
        BigDecimal antes = saldo(ANA);

        mvc.perform(post("/quinielas/{id}/boleto", quiniela.getId()).with(user(ANA)).with(csrf())
                        .param("p0", "LOCAL").param("p1", "EMPATE").param("p2", "VISITANTE"))
                .andExpect(flash().attribute("mensaje", "Boleto echado. ¡Suerte!"));

        assertThat(saldo(ANA)).isEqualByComparingTo(antes.subtract(BigDecimal.TEN));
        assertThat(movimientosDe(ANA, TipoMovimiento.QUINIELA_BOLETO)).hasSize(1);
        assertThat(quinielas.boletoDe(ANA, quiniela.getId()).orElseThrow().getPronosticos()).isEqualTo("1X2");
        mvc.perform(get("/quinielas/{id}", quiniela.getId()).with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mi pronóstico")));

        mvc.perform(post("/quinielas/{id}/boleto", quiniela.getId()).with(user(ANA)).with(csrf())
                        .param("p0", "LOCAL").param("p1", "LOCAL").param("p2", "LOCAL"))
                .andExpect(flash().attribute("error", "Ya has echado tu boleto en esta quiniela"));
        assertThat(saldo(ANA)).isEqualByComparingTo(antes.subtract(BigDecimal.TEN));
    }

    @Test
    void faltaUnPronosticoYNoSeAdmite() {
        Quiniela quiniela = crear();
        assertThatThrownBy(() -> quinielas.echarBoleto(ANA, quiniela.getId(), java.util.Arrays.asList(Resultado.LOCAL, null, Resultado.LOCAL)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Elige 1, X o 2 en cada partido");
    }

    @Test
    void importeDeBoletoRespetaLimitesDeJuegoResponsable() {
        Quiniela quiniela = quinielas.crear("Boleto caro", new BigDecimal("100000"),
                List.of(primero.getId(), segundo.getId(), tercero.getId()));
        assertThatThrownBy(() -> quinielas.echarBoleto(ANA, quiniela.getId(),
                List.of(Resultado.LOCAL, Resultado.EMPATE, Resultado.VISITANTE)))
                .isInstanceOf(RuntimeException.class);
        assertThat(quinielas.boletoDe(ANA, quiniela.getId())).isEmpty();
    }

    @Test
    void pronosticoWebDesconocidoSeDevuelveComoErrorYNoRompeLaPeticion() throws Exception {
        Quiniela quiniela = crear();
        mvc.perform(post("/quinielas/{id}/boleto", quiniela.getId()).with(user(ANA)).with(csrf())
                        .param("p0", "LOCAL").param("p1", "EMPATE").param("p2", "EMPATE"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/quinielas/{id}/boleto", quiniela.getId()).with(user(BEA)).with(csrf())
                        .param("p0", "LOCAL").param("p1", "INVALIDO").param("p2", "VISITANTE"))
                .andExpect(flash().attribute("error", "No enum constant es.ucm.fdi.is1.apuestas.cuotas.Resultado.INVALIDO"));
        assertThat(quinielas.boletoDe(BEA, quiniela.getId())).isEmpty();
    }

    @Test
    void siEmpiezaOSeSuspendeUnPartidoLaQuinielaSeCierra() {
        Quiniela quiniela = crear();
        resolucion.suspender(segundo.getId());
        assertThatThrownBy(() -> quinielas.echarBoleto(ANA, quiniela.getId(), List.of(Resultado.LOCAL, Resultado.LOCAL, Resultado.LOCAL)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("La quiniela está cerrada");

        resolucion.reactivar(segundo.getId());
        primero.cambiarFecha(Hora.ahora().minusMinutes(1));
        assertThat(quinielas.resumen(quiniela.getId()).admiteBoletos()).isFalse();
        assertThatThrownBy(() -> quinielas.echarBoleto(ANA, quiniela.getId(), List.of(Resultado.LOCAL, Resultado.LOCAL, Resultado.LOCAL)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void elCreadorNoPuedeJugar() throws Exception {
        Quiniela quiniela = crear();
        mvc.perform(get("/quinielas/{id}", quiniela.getId()).with(CREADOR))
                .andExpect(content().string(containsString("El creador de apuestas no juega a la quiniela")));
        assertThatThrownBy(() -> quinielas.echarBoleto("creador@apuestas.es", quiniela.getId(),
                List.of(Resultado.LOCAL, Resultado.LOCAL, Resultado.LOCAL)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("no puede jugar");
    }

    /**
     * Echar un boleto sube la versión de la quiniela: si a la vez se anula, una de las dos falla, en lugar de quedar un
     * boleto cobrado en una quiniela anulada al que nadie le devuelve el precio.
     */
    @Test
    void echarUnBoletoSubeLaVersionDeLaQuiniela() {
        Quiniela quiniela = crear();
        long antes = version(quiniela);
        // Como en la aplicación, donde cada petición carga la quiniela de nuevo
        em.clear();
        quinielas.echarBoleto(ANA, quiniela.getId(), List.of(Resultado.LOCAL, Resultado.EMPATE, Resultado.VISITANTE));
        assertThat(version(quiniela)).isGreaterThan(antes);
    }

    @Test
    void laQuinielaSeVeSinIniciarSesion() throws Exception {
        Quiniela quiniela = crear();
        mvc.perform(get("/quinielas")).andExpect(status().isOk()).andExpect(content().string(containsString("Jornada 14")));
        // Con sesión, el formulario del boleto lleva su token CSRF
        mvc.perform(get("/quinielas/{id}", quiniela.getId()).with(user(ANA)))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        var resultado = mvc.perform(get("/quinielas/{id}", quiniela.getId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Inicia sesión")))
                .andExpect(content().string(containsString("</html>")))
                .andReturn();
        // Sin formulario que enviar no hace falta el token CSRF, que abriría una sesión a mitad de página
        assertThat(resultado.getRequest().getSession(false)).isNull();
    }

    // --- HU-80 ---

    @Test
    void elBoteSeRepartePorIgualEntreLosQueMasAciertanYTodosRecibenAviso() throws Exception {
        Quiniela quiniela = crear();
        quinielas.echarBoleto(ANA, quiniela.getId(), List.of(Resultado.LOCAL, Resultado.EMPATE, Resultado.VISITANTE));
        quinielas.echarBoleto(BEA, quiniela.getId(), List.of(Resultado.LOCAL, Resultado.EMPATE, Resultado.LOCAL));
        quinielas.echarBoleto(CARLA, quiniela.getId(), List.of(Resultado.LOCAL, Resultado.EMPATE, Resultado.VISITANTE));
        BigDecimal anaAntes = saldo(ANA);
        BigDecimal beaAntes = saldo(BEA);

        assertThatThrownBy(() -> quinielas.repartir(quiniela.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessage("Faltan partidos por jugar");
        resolucion.introducirResultado(primero.getId(), Resultado.LOCAL);
        resolucion.introducirResultado(segundo.getId(), Resultado.EMPATE);
        resolucion.introducirResultado(tercero.getId(), Resultado.VISITANTE);

        mvc.perform(post("/gestion/quinielas/{id}/repartir", quiniela.getId()).with(CREADOR).with(csrf()))
                .andExpect(flash().attribute("mensaje", "Bote de «Jornada 14» repartido."));

        // 30 pagados menos el 10 %: 27, para Ana y Carla con 3 aciertos
        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes.add(new BigDecimal("13.50")));
        assertThat(saldo(BEA)).isEqualByComparingTo(beaAntes);
        assertThat(movimientosDe(ANA, TipoMovimiento.QUINIELA_PREMIO)).hasSize(1);
        assertThat(quinielas.boletoDe(BEA, quiniela.getId()).orElseThrow().getAciertos()).isEqualTo(2);
        assertThat(notificaciones.leer(ANA).get(0).texto())
                .isEqualTo("¡Has ganado 13,50 monedas en la quiniela «Jornada 14» con 3 de 3 aciertos!");
        assertThat(notificaciones.leer(BEA).get(0).texto()).contains("has acertado 2 de 3");
        assertThat(quinielas.quiniela(quiniela.getId()).getEstado()).isEqualTo(EstadoQuiniela.REPARTIDA);
        mvc.perform(get("/quinielas/{id}", quiniela.getId()).with(user(ANA)))
                .andExpect(content().string(containsString("Reparto")))
                .andExpect(content().string(containsString("13,50")));

        assertThatThrownBy(() -> quinielas.repartir(quiniela.getId())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noSePuedeRepartirNiAnularUnaQuinielaYaRepartida() {
        Quiniela quiniela = crear();
        resolucion.introducirResultado(primero.getId(), Resultado.LOCAL);
        resolucion.introducirResultado(segundo.getId(), Resultado.EMPATE);
        resolucion.introducirResultado(tercero.getId(), Resultado.VISITANTE);
        quinielas.repartir(quiniela.getId());
        assertThatThrownBy(() -> quinielas.repartir(quiniela.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessage("Esta quiniela ya está repartida");
        assertThatThrownBy(() -> quinielas.anular(quiniela.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessage("Esta quiniela ya está repartida");
    }

    @Test
    void unPartidoAnuladoCuentaComoAciertoParaTodos() {
        Quiniela quiniela = crear();
        quinielas.echarBoleto(ANA, quiniela.getId(), List.of(Resultado.LOCAL, Resultado.LOCAL, Resultado.LOCAL));
        quinielas.echarBoleto(BEA, quiniela.getId(), List.of(Resultado.VISITANTE, Resultado.VISITANTE, Resultado.LOCAL));
        BigDecimal beaAntes = saldo(BEA);
        resolucion.introducirResultado(primero.getId(), Resultado.LOCAL);
        resolucion.anular(segundo.getId());
        resolucion.introducirResultado(tercero.getId(), Resultado.LOCAL);

        quinielas.repartir(quiniela.getId());

        assertThat(quinielas.boletoDe(ANA, quiniela.getId()).orElseThrow().getAciertos()).isEqualTo(3);
        assertThat(quinielas.boletoDe(BEA, quiniela.getId()).orElseThrow().getAciertos()).isEqualTo(2);
        assertThat(saldo(BEA)).isEqualByComparingTo(beaAntes);
    }

    @Test
    void siNadieAciertaNingunoSeDevuelveTodo() {
        Quiniela quiniela = crear();
        quinielas.echarBoleto(ANA, quiniela.getId(), List.of(Resultado.VISITANTE, Resultado.VISITANTE, Resultado.VISITANTE));
        BigDecimal anaAntes = saldo(ANA);
        resolucion.introducirResultado(primero.getId(), Resultado.LOCAL);
        resolucion.introducirResultado(segundo.getId(), Resultado.LOCAL);
        resolucion.introducirResultado(tercero.getId(), Resultado.EMPATE);

        quinielas.repartir(quiniela.getId());

        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes.add(BigDecimal.TEN));
        assertThat(movimientosDe(ANA, TipoMovimiento.DEVOLUCION)).hasSize(1);
        assertThat(quinielas.boletoDe(ANA, quiniela.getId()).orElseThrow().isDevuelto()).isTrue();
    }

    /** Crear una quiniela con un error devuelve lo escrito, para corregir solo lo que falla. */
    @Test
    void unErrorAlCrearLaQuinielaConservaLoEscrito() throws Exception {
        mvc.perform(post("/gestion/quinielas").with(CREADOR).with(csrf())
                        .param("nombre", "Prueba Q").param("precio", "3.50")
                        .param("eventos", primero.getId().toString(), segundo.getId().toString()))
                .andExpect(flash().attribute("error", containsString("3")))
                .andExpect(flash().attribute("nombreEscrito", "Prueba Q"))
                .andExpect(flash().attribute("eventosElegidos", List.of(primero.getId(), segundo.getId())));
    }

    /**
     * El panel del creador pinta las quinielas fuera de la transacción: sus partidos tienen que venir ya cargados
     * aunque esté repartida o anulada. Antes solo se cargaban en las abiertas y la página daba error 500.
     */
    @Test
    void elPanelTraeLosPartidosDeLasQuinielasRepartidasYAnuladas() {
        Quiniela anulada = crear();
        quinielas.anular(anulada.getId());
        em.flush();
        em.clear();

        QuinielaService.Resumen resumen = quinielas.todas().stream()
                .filter(r -> r.quiniela().getId().equals(anulada.getId())).findFirst().orElseThrow();
        assertThat(org.hibernate.Hibernate.isInitialized(resumen.quiniela().getPartidos())).isTrue();
    }

    /** Con todos los partidos anulados no se ha jugado nada: se devuelve el precio entero, sin comisión de la casa. */
    @Test
    void siSeAnulanTodosLosPartidosSeDevuelveElPrecioEntero() {
        Quiniela quiniela = crear();
        quinielas.echarBoleto(ANA, quiniela.getId(), List.of(Resultado.LOCAL, Resultado.LOCAL, Resultado.LOCAL));
        quinielas.echarBoleto(BEA, quiniela.getId(), List.of(Resultado.EMPATE, Resultado.EMPATE, Resultado.EMPATE));
        BigDecimal anaAntes = saldo(ANA);
        for (Evento partido : List.of(primero, segundo, tercero)) {
            resolucion.anular(partido.getId());
        }

        quinielas.repartir(quiniela.getId());

        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes.add(BigDecimal.TEN));
        assertThat(movimientosDe(ANA, TipoMovimiento.QUINIELA_PREMIO)).isEmpty();
    }

    /** La hora de cierre es la del partido que empieza antes, aunque se haya aplazado el que iba primero. */
    @Test
    void elCierreEsElDelPartidoQueEmpiezaAntesAunqueSeAplaceElPrimero() {
        Quiniela quiniela = crear();
        primero.cambiarFecha(primero.getFechaHora().plusDays(7));

        assertThat(quinielas.quiniela(quiniela.getId()).getCierre()).isEqualTo(segundo.getFechaHora());
    }

    @Test
    void alAnularlaAntesDeRepartirSeDevuelveElPrecio() throws Exception {
        Quiniela quiniela = crear();
        quinielas.echarBoleto(ANA, quiniela.getId(), List.of(Resultado.LOCAL, Resultado.LOCAL, Resultado.LOCAL));
        BigDecimal anaAntes = saldo(ANA);

        mvc.perform(post("/gestion/quinielas/{id}/anular", quiniela.getId()).with(CREADOR).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes.add(BigDecimal.TEN));
        assertThat(notificaciones.leer(ANA).get(0).texto())
                .isEqualTo("Se ha anulado la quiniela «Jornada 14»: te hemos devuelto 10,00 monedas.");
        assertThat(quinielas.quiniela(quiniela.getId()).getEstado()).isEqualTo(EstadoQuiniela.ANULADA);
        assertThatThrownBy(() -> quinielas.anular(quiniela.getId())).isInstanceOf(IllegalStateException.class);
    }
}

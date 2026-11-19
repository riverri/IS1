package es.ucm.fdi.is1.apuestas.ligas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
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

/** HU-89: retos 1 contra 1 entre compañeros de liga. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RetoWebTest {

    private static final String ANA = "ana-reto@ucm.es";
    private static final String LUIS = "luis-reto@ucm.es";
    private static final String EVA = "eva-reto@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RetoService retos;

    @Autowired
    private es.ucm.fdi.is1.apuestas.apuesta.BajaService bajas;

    @Autowired
    private LigaService ligas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private es.ucm.fdi.is1.apuestas.gestion.GestionService gestion;

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

    private Usuario luis;
    private Liga liga;
    private Evento partido;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        luis = usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        usuarioService.crear(EVA, "Eva", "secreta123", Rol.USUARIO);
        liga = ligas.crear(ANA, "Los del reto");
        ligas.unirse(LUIS, liga.getCodigo());
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
    }

    private BigDecimal saldo(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo();
    }

    @Test
    void retoAUnCompaneroDesdeLaLigaYLeLlegaElAviso() throws Exception {
        BigDecimal antes = saldo(ANA);

        mvc.perform(post("/retos").with(user(ANA)).with(csrf()).header("Referer", "http://localhost/ligas/" + liga.getId())
                        .param("retado", luis.getId().toString()).param("evento", partido.getId().toString())
                        .param("pronostico", "LOCAL").param("importe", "10"))
                .andExpect(flash().attribute("mensaje", containsString("Reto enviado")));

        assertThat(saldo(ANA)).isEqualByComparingTo(antes.subtract(BigDecimal.TEN));
        assertThat(notificaciones.leer(LUIS).get(0).texto()).startsWith("Ana te reta en Getafe CF – Sevilla FC");
        mvc.perform(get("/ligas/{id}", liga.getId()).with(user(LUIS)))
                .andExpect(content().string(containsString("Retos 1 contra 1")))
                .andExpect(content().string(containsString("Aceptar")));
    }

    @Test
    void unEventoConRetosNoSeBorra() {
        retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        assertThatThrownBy(() -> gestion.borrarEvento(partido.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("retos");
    }

    @Test
    void soloSeRetaACompanerosDeLiga() {
        Long eva = usuarios.findByEmail(EVA).orElseThrow().getId();
        assertThatThrownBy(() -> retos.retar(ANA, eva, partido.getId(), Resultado.LOCAL, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("tus ligas");
    }

    @Test
    void elQueAciertaSeLlevaLosDosImportes() {
        Reto reto = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        assertThatThrownBy(() -> retos.aceptar(LUIS, reto.getId(), Resultado.LOCAL))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("distinto");
        retos.aceptar(LUIS, reto.getId(), Resultado.VISITANTE);
        BigDecimal ana = saldo(ANA);
        BigDecimal luisAntes = saldo(LUIS);

        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);

        assertThat(reto.getEstado()).isEqualTo(EstadoReto.RESUELTO);
        assertThat(reto.getGanador().getEmail()).isEqualTo(ANA);
        assertThat(saldo(ANA)).isEqualByComparingTo(ana.add(new BigDecimal("20")));
        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes);
        assertThat(notificaciones.leer(LUIS).get(0).texto()).startsWith("Has perdido el reto contra Ana");
    }

    @Test
    void siNoAciertaNingunoSeDevuelveACadaUnoLoSuyo() {
        Reto reto = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        retos.aceptar(LUIS, reto.getId(), Resultado.VISITANTE);
        BigDecimal ana = saldo(ANA);
        BigDecimal luisAntes = saldo(LUIS);

        resolucion.introducirResultado(partido.getId(), Resultado.EMPATE);

        assertThat(reto.getEstado()).isEqualTo(EstadoReto.DEVUELTO);
        assertThat(saldo(ANA)).isEqualByComparingTo(ana.add(BigDecimal.TEN));
        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes.add(BigDecimal.TEN));
    }

    @Test
    void rechazarloOQueEmpieceSinAceptarDevuelveElImporte() {
        BigDecimal antes = saldo(ANA);
        Reto rechazado = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        retos.rechazar(LUIS, rechazado.getId());
        assertThat(rechazado.getEstado()).isEqualTo(EstadoReto.RECHAZADO);
        assertThat(saldo(ANA)).isEqualByComparingTo(antes);

        Reto sinContestar = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        resolucion.anular(partido.getId());
        assertThat(sinContestar.getEstado()).isEqualTo(EstadoReto.DEVUELTO);
        assertThat(saldo(ANA)).isEqualByComparingTo(antes);
    }

    @Test
    void elRetadoAceptaDesdeLaWebConOtroResultado() throws Exception {
        Reto reto = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        mvc.perform(post("/retos/{id}/aceptar", reto.getId()).with(user(LUIS)).with(csrf()).param("pronostico", "LOCAL"))
                .andExpect(flash().attribute("error", containsString("Elige un resultado distinto del de Ana")));
        mvc.perform(post("/retos/{id}/aceptar", reto.getId()).with(user(EVA)).with(csrf()).param("pronostico", "VISITANTE"))
                .andExpect(flash().attribute("error", "Este reto no es para ti"));

        BigDecimal antes = saldo(LUIS);
        mvc.perform(post("/retos/{id}/aceptar", reto.getId()).with(user(LUIS)).with(csrf()).param("pronostico", "VISITANTE"))
                .andExpect(flash().attribute("mensaje", "Reto aceptado. ¡Suerte!"));
        assertThat(reto.getEstado()).isEqualTo(EstadoReto.ACEPTADO);
        assertThat(saldo(LUIS)).isEqualByComparingTo(antes.subtract(BigDecimal.TEN));

        // Ya aceptado, ni se vuelve a aceptar ni se puede retirar
        mvc.perform(post("/retos/{id}/aceptar", reto.getId()).with(user(LUIS)).with(csrf()).param("pronostico", "EMPATE"))
                .andExpect(flash().attribute("error", "Este reto ya no está pendiente"));
        mvc.perform(post("/retos/{id}/rechazar", reto.getId()).with(user(ANA)).with(csrf()))
                .andExpect(flash().attribute("error", "Este reto ya no está pendiente"));
    }

    @Test
    void elRetadorLoRetiraDesdeLaWebYNadieMasPuede() throws Exception {
        Reto reto = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        mvc.perform(post("/retos/{id}/rechazar", reto.getId()).with(user(EVA)).with(csrf()))
                .andExpect(flash().attribute("error", "Este reto no es tuyo"));
        mvc.perform(post("/retos/{id}/rechazar", reto.getId()).with(user(ANA)).with(csrf()))
                .andExpect(flash().attribute("mensaje", containsString("se ha devuelto el importe")));
        assertThat(reto.getEstado()).isEqualTo(EstadoReto.RECHAZADO);
        mvc.perform(post("/retos/{id}/aceptar", 999_999).with(user(LUIS)).with(csrf()).param("pronostico", "VISITANTE"))
                .andExpect(flash().attribute("error", "No existe ese reto"));
    }

    @Test
    void sinImporteOSinSaldoNoSeReta() throws Exception {
        mvc.perform(post("/retos").with(user(ANA)).with(csrf()).param("retado", luis.getId().toString())
                        .param("evento", partido.getId().toString()).param("pronostico", "LOCAL"))
                .andExpect(flash().attribute("error", "Indica el importe"));
        mvc.perform(post("/retos").with(user(ANA)).with(csrf()).param("retado", luis.getId().toString())
                        .param("evento", partido.getId().toString()).param("importe", "10"))
                .andExpect(flash().attribute("error", containsString("Elige un resultado")));
        // Ana se queda sin saldo con dos retos del importe máximo
        retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, new BigDecimal("500"));
        retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, new BigDecimal("500"));
        mvc.perform(post("/retos").with(user(ANA)).with(csrf()).param("retado", luis.getId().toString())
                        .param("evento", partido.getId().toString()).param("pronostico", "LOCAL").param("importe", "10"))
                .andExpect(flash().attribute("error", "No tienes saldo suficiente para el reto"));
    }

    @Test
    void siSeCorrigeElResultadoElRetoSeRecalcula() {
        BigDecimal anaAntes = saldo(ANA);
        BigDecimal luisAntes = saldo(LUIS);
        Reto reto = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        retos.aceptar(LUIS, reto.getId(), Resultado.VISITANTE);
        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);
        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes.add(BigDecimal.TEN));

        // El creador se equivocó: ganó el visitante. Ana devuelve el bote y lo cobra Luis
        resolucion.introducirResultado(partido.getId(), Resultado.VISITANTE);
        assertThat(reto.getEstado()).isEqualTo(EstadoReto.RESUELTO);
        assertThat(reto.getGanador().getEmail()).isEqualTo(LUIS);
        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes.subtract(BigDecimal.TEN));
        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes.add(BigDecimal.TEN));

        // Empate: nadie acierta y se devuelve a cada uno lo suyo
        resolucion.introducirResultado(partido.getId(), Resultado.EMPATE);
        assertThat(reto.getEstado()).isEqualTo(EstadoReto.DEVUELTO);
        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes);
        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes);

        // Volver a guardar el mismo resultado no cambia nada
        resolucion.introducirResultado(partido.getId(), Resultado.EMPATE);
        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes);
    }

    @Test
    void siSeAnulaUnPartidoYaResueltoSeDevuelveElReto() {
        BigDecimal anaAntes = saldo(ANA);
        BigDecimal luisAntes = saldo(LUIS);
        Reto reto = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        retos.aceptar(LUIS, reto.getId(), Resultado.VISITANTE);
        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);
        resolucion.anular(partido.getId());
        assertThat(reto.getEstado()).isEqualTo(EstadoReto.DEVUELTO);
        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes);
        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes);
    }

    @Test
    void siDejanDeCompartirLigaLosRetosPendientesSeDevuelven() {
        BigDecimal antes = saldo(ANA);
        Reto alSalir = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        ligas.salir(liga.getId(), LUIS);
        assertThat(alSalir.getEstado()).isEqualTo(EstadoReto.DEVUELTO);
        assertThat(saldo(ANA)).isEqualByComparingTo(antes);

        // Si comparten otra liga, el reto sigue (se ve y se contesta desde esa)
        Liga otra = ligas.crear(ANA, "Otra liga");
        ligas.unirse(LUIS, otra.getCodigo());
        ligas.unirse(LUIS, liga.getCodigo());
        Reto sigue = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        ligas.salir(liga.getId(), LUIS);
        assertThat(sigue.getEstado()).isEqualTo(EstadoReto.PENDIENTE);

        // Al borrar la última liga en común, también se devuelve
        ligas.borrar(otra.getId(), ANA);
        assertThat(sigue.getEstado()).isEqualTo(EstadoReto.DEVUELTO);
        assertThat(saldo(ANA)).isEqualByComparingTo(antes);
    }

    /** Como al salir de la liga: si el retado elimina su cuenta sin contestar, al retador se le devuelve el importe. */
    @Test
    void siElRetadoEliminaSuCuentaElRetoPendienteSeDevuelve() {
        BigDecimal antes = saldo(ANA);
        Reto reto = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);

        bajas.eliminarCuenta(LUIS, "secreta123");

        assertThat(reto.getEstado()).isEqualTo(EstadoReto.DEVUELTO);
        assertThat(saldo(ANA)).isEqualByComparingTo(antes);
    }

    /**
     * Si después de resolverse uno de los dos elimina su cuenta y se corrige el resultado, el reto se queda como se
     * cerró: a la cuenta eliminada no se le puede retirar nada, y pagar al otro crearía dinero de la nada.
     */
    @Test
    void corregirUnRetoConUnaCuentaEliminadaNoCreaDinero() {
        Reto reto = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        retos.aceptar(LUIS, reto.getId(), Resultado.VISITANTE);
        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);
        assertThat(reto.getGanador().getEmail()).isEqualTo(ANA);

        bajas.eliminarCuenta(ANA, "secreta123");
        BigDecimal luisAntes = saldo(LUIS);
        resolucion.introducirResultado(partido.getId(), Resultado.VISITANTE);

        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes);
        assertThat(reto.getEstado()).isEqualTo(EstadoReto.RESUELTO);
    }

    @Test
    void elImporteDelRetoLlevaDosDecimalesComoMucho() {
        assertThatThrownBy(() -> retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, new BigDecimal("1.005")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("2 decimales");
        assertThatThrownBy(() -> retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, new BigDecimal("-5")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

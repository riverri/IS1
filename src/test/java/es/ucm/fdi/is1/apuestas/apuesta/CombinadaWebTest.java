package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-28, HU-29 y HU-30: boleto, multiplicador total y resolución de combinadas. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CombinadaWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA);
    private static final RequestPostProcessor CREADOR = user("creador@apuestas.es").roles("CREADOR");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ApuestaRepository apuestas;

    @Autowired
    private CalculadoraCuotas calculadora;

    private MockHttpSession sesion;
    private Evento futbol;
    private Evento basket;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        sesion = new MockHttpSession();
        LocalDateTime manana = Hora.ahora().plusDays(1);
        futbol = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), manana));
        basket = eventos.save(new Evento(competiciones.findByNombre("Liga ACB").orElseThrow(),
                equipos.findByNombre("Unicaja").orElseThrow(),
                equipos.findByNombre("Baskonia").orElseThrow(), manana));
    }

    private void anadir(Evento evento, String resultado) throws Exception {
        mvc.perform(post("/boleto/anadir").session(sesion).with(COMO_ANA).with(csrf())
                        .param("evento", evento.getId().toString()).param("resultado", resultado))
                .andExpect(status().is3xxRedirection());
    }

    private BigDecimal cuota(Evento evento, Resultado resultado) {
        return calculadora.calcular(evento).de(resultado);
    }

    private BigDecimal producto(BigDecimal a, BigDecimal b) {
        return a.multiply(b).setScale(2, RoundingMode.DOWN);
    }

    private BigDecimal saldo() {
        return usuarios.findByEmail(ANA).orElseThrow().getSaldo();
    }

    private Apuesta confirmar(String importe) throws Exception {
        mvc.perform(post("/boleto/confirmar").session(sesion).with(COMO_ANA).with(csrf()).param("importe", importe))
                .andExpect(redirectedUrl("/apuestas"));
        return apuestas.findAll().stream().filter(a -> a.getUsuario().getEmail().equals(ANA)).findFirst().orElseThrow();
    }

    private void resultado(Evento evento, String resultado) throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/resultado", evento.getId()).with(CREADOR).with(csrf())
                .param("resultado", resultado));
    }

    @Test
    void elBoletoMuestraElMultiplicadorTotal() throws Exception {
        anadir(futbol, "LOCAL");
        anadir(basket, "VISITANTE");
        BigDecimal total = producto(cuota(futbol, Resultado.LOCAL), cuota(basket, Resultado.VISITANTE));

        mvc.perform(get("/boleto").session(sesion).with(COMO_ANA))
                .andExpect(content().string(containsString("Combinada de 2 selecciones")))
                .andExpect(content().string(containsString("× " + total.toPlainString().replace('.', ','))));
    }

    @Test
    void noSePuedenCombinarDosSeleccionesDelMismoEvento() throws Exception {
        anadir(futbol, "LOCAL");
        mvc.perform(post("/boleto/anadir").session(sesion).with(COMO_ANA).with(csrf())
                        .param("evento", futbol.getId().toString()).param("resultado", "EMPATE"))
                .andExpect(flash().attribute("errorBoleto", containsString("Ya tienes una selección de ese partido")));
    }

    @Test
    void siSoloQuedaUnaSeleccionEsUnaApuestaSimple() throws Exception {
        anadir(futbol, "LOCAL");
        anadir(basket, "LOCAL");
        mvc.perform(post("/boleto/quitar/{id}", basket.getId()).session(sesion).with(COMO_ANA).with(csrf()));

        mvc.perform(get("/boleto").session(sesion).with(COMO_ANA))
                .andExpect(content().string(containsString("Apuesta simple")));
    }

    @Test
    void confirmarCreaLaCombinadaYDescuentaElSaldo() throws Exception {
        anadir(futbol, "EMPATE");
        anadir(basket, "LOCAL");
        BigDecimal total = producto(cuota(futbol, Resultado.EMPATE), cuota(basket, Resultado.LOCAL));

        Apuesta combinada = confirmar("20");

        assertThat(combinada.isCombinada()).isTrue();
        assertThat(combinada.getSelecciones()).hasSize(2);
        assertThat(combinada.getCuota()).isEqualByComparingTo(total);
        assertThat(saldo()).isEqualByComparingTo("980");
        mvc.perform(get("/boleto").session(sesion).with(COMO_ANA))
                .andExpect(content().string(containsString("Tu boleto está vacío")));
    }

    @Test
    void siCambiaUnaCuotaSePideAceptarla() throws Exception {
        anadir(futbol, "LOCAL");
        equipos.findByNombre("Getafe CF").orElseThrow().setCalidad(9.5);

        mvc.perform(post("/boleto/confirmar").session(sesion).with(COMO_ANA).with(csrf()).param("importe", "10"))
                .andExpect(redirectedUrl("/boleto"))
                .andExpect(flash().attributeExists("aviso"));
        assertThat(apuestas.count()).isZero();
        assertThat(saldo()).isEqualByComparingTo("1000");

        Apuesta apuesta = confirmar("10");
        assertThat(apuesta.getCuota()).isEqualByComparingTo(cuota(futbol, Resultado.LOCAL));
    }

    @Test
    void laCombinadaSePierdeEnCuantoFallaUnaSeleccion() throws Exception {
        anadir(futbol, "LOCAL");
        anadir(basket, "LOCAL");
        Apuesta combinada = confirmar("10");

        resultado(futbol, "VISITANTE");

        assertThat(combinada.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
        assertThat(saldo()).isEqualByComparingTo("990");
    }

    @Test
    void laCombinadaSeGanaSiAciertanTodas() throws Exception {
        anadir(futbol, "LOCAL");
        anadir(basket, "VISITANTE");
        Apuesta combinada = confirmar("10");

        resultado(futbol, "LOCAL");
        assertThat(combinada.getEstado()).isEqualTo(EstadoApuesta.ACTIVA);
        resultado(basket, "VISITANTE");

        assertThat(combinada.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        assertThat(saldo()).isEqualByComparingTo(new BigDecimal("990")
                .add(new BigDecimal("10").multiply(combinada.getCuota()).setScale(2, RoundingMode.DOWN)));
    }

    @Test
    void unEventoAnuladoCuentaConCuotaUno() throws Exception {
        anadir(futbol, "LOCAL");
        anadir(basket, "LOCAL");
        Apuesta combinada = confirmar("10");
        BigDecimal cuotaBasket = combinada.getSelecciones().stream()
                .filter(s -> s.getEvento().getId().equals(basket.getId())).findFirst().orElseThrow().getCuota();

        mvc.perform(post("/gestion/eventos/{id}/anular", futbol.getId()).with(CREADOR).with(csrf()));
        assertThat(combinada.getEstado()).isEqualTo(EstadoApuesta.ACTIVA);
        resultado(basket, "LOCAL");

        assertThat(combinada.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        assertThat(saldo()).isEqualByComparingTo(new BigDecimal("990")
                .add(new BigDecimal("10").multiply(cuotaBasket).setScale(2, RoundingMode.DOWN)));
    }

    @Test
    void corregirUnResultadoRecalculaLaCombinada() throws Exception {
        anadir(futbol, "LOCAL");
        anadir(basket, "LOCAL");
        Apuesta combinada = confirmar("10");

        resultado(basket, "LOCAL");
        resultado(futbol, "VISITANTE");
        assertThat(combinada.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);

        resultado(futbol, "LOCAL");
        assertThat(combinada.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        resultado(futbol, "EMPATE");
        assertThat(combinada.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
        assertThat(saldo()).isEqualByComparingTo("990");
    }

    @Test
    void unVisitanteQueAnadeAlBoletoVaAlLogin() throws Exception {
        mvc.perform(post("/boleto/anadir").with(csrf()).param("evento", futbol.getId().toString()).param("resultado", "LOCAL"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void soloVuelveAPaginasDeEstaWeb() throws Exception {
        mvc.perform(post("/boleto/anadir").session(sesion).with(COMO_ANA).with(csrf())
                        .header("Referer", "https://otra-web.com/robar")
                        .param("evento", futbol.getId().toString()).param("resultado", "LOCAL"))
                .andExpect(redirectedUrl("/robar"));
        mvc.perform(post("/boleto/anadir").session(sesion).with(COMO_ANA).with(csrf())
                        .header("Referer", "http://localhost/eventos?deporte=FUTBOL")
                        .param("evento", basket.getId().toString()).param("resultado", "LOCAL"))
                .andExpect(redirectedUrl("/eventos?deporte=FUTBOL"));
    }
}

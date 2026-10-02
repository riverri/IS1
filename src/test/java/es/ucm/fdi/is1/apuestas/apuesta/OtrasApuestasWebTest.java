package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-52: doble oportunidad, más/menos de 2,5 goles y ambos marcan. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OtrasApuestasWebTest {

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

    private Evento futbol;
    private Evento basket;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        LocalDateTime manana = LocalDateTime.now().plusDays(1);
        futbol = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), manana));
        basket = eventos.save(new Evento(competiciones.findByNombre("Liga ACB").orElseThrow(),
                equipos.findByNombre("Unicaja").orElseThrow(),
                equipos.findByNombre("Baskonia").orElseThrow(), manana));
    }

    private Apuesta apostar(Evento evento, String opcion) throws Exception {
        mvc.perform(post("/eventos/{id}/apostar", evento.getId()).with(COMO_ANA).with(csrf())
                        .param("opcion", opcion).param("importe", "10"))
                .andExpect(redirectedUrl("/apuestas"));
        return apuestas.findAll().stream().filter(a -> a.getUsuario().getEmail().equals(ANA))
                .reduce((primera, segunda) -> segunda).orElseThrow();
    }

    private BigDecimal saldo() {
        return usuarios.findByEmail(ANA).orElseThrow().getSaldo();
    }

    @Test
    void laPaginaDelPartidoMuestraLosOtrosTiposSoloEnFutbol() throws Exception {
        String masDe = calculadora.especiales(futbol).get(Especial.MAS_2_5).toPlainString().replace('.', ',');
        mvc.perform(get("/eventos/{id}/apostar", futbol.getId()).with(COMO_ANA))
                .andExpect(content().string(containsString("Doble oportunidad")))
                .andExpect(content().string(containsString("Más de 2,5 goles")))
                .andExpect(content().string(containsString("Ambos marcan")))
                .andExpect(content().string(containsString(masDe)));
        mvc.perform(get("/eventos/{id}/apostar", basket.getId()).with(COMO_ANA))
                .andExpect(content().string(not(containsString("Más de 2,5 goles"))));
    }

    @Test
    void conElMarcadorSeResuelvenLasDeGoles() throws Exception {
        Apuesta mas = apostar(futbol, "MAS_2_5");
        BigDecimal cuota = mas.getCuota();
        assertThat(mas.getSelecciones().get(0).getEspecial()).isEqualTo(Especial.MAS_2_5);
        assertThat(cuota).isEqualTo(calculadora.especiales(futbol).get(Especial.MAS_2_5));
        BigDecimal antes = saldo();
        // Las apuestas de goles no entran en el ajuste por volumen del 1X2
        mvc.perform(get("/eventos/{id}/apostar", futbol.getId()).with(COMO_ANA)).andExpect(status().isOk());

        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                        .param("golesLocal", "2").param("golesVisitante", "1"))
                .andExpect(flash().attribute("mensaje", containsString("2-1")));

        assertThat(mas.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        assertThat(saldo()).isEqualByComparingTo(antes.add(new BigDecimal("10").multiply(cuota)));
        assertThat(futbol.getResultado()).isEqualTo(es.ucm.fdi.is1.apuestas.cuotas.Resultado.LOCAL);
    }

    @Test
    void sinMarcadorLasDeGolesSeAnulanYLasDeDobleOportunidadSeResuelven() throws Exception {
        Apuesta ambos = apostar(futbol, "AMBOS_SI");
        Evento otro = eventos.save(new Evento(futbol.getCompeticion(), futbol.getVisitante(), futbol.getLocal(),
                futbol.getFechaHora()));
        Apuesta doble = apostar(otro, "DOBLE_X2");
        BigDecimal antes = saldo();

        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                .param("resultado", "LOCAL"));
        mvc.perform(post("/gestion/eventos/{id}/resultado", otro.getId()).with(CREADOR).with(csrf())
                .param("resultado", "LOCAL"));

        assertThat(ambos.getEstado()).isEqualTo(EstadoApuesta.ANULADA);
        assertThat(doble.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
        assertThat(saldo()).isEqualByComparingTo(antes.add(new BigDecimal("10")));
    }

    @Test
    void corregirElMarcadorVuelveAResolver() throws Exception {
        Apuesta menos = apostar(futbol, "MENOS_2_5");
        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                .param("golesLocal", "3").param("golesVisitante", "1"));
        assertThat(menos.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
        BigDecimal antes = saldo();

        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                .param("golesLocal", "1").param("golesVisitante", "0"));

        assertThat(menos.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        assertThat(saldo()).isGreaterThan(antes);
    }

    @Test
    void seCombinanConOtrosPartidos() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(post("/boleto/anadir").session(sesion).with(COMO_ANA).with(csrf())
                        .param("evento", futbol.getId().toString()).param("opcion", "DOBLE_1X")
                        .header("Referer", "http://localhost/eventos/" + futbol.getId() + "/apostar"))
                .andExpect(redirectedUrl("/eventos/" + futbol.getId() + "/apostar"))
                .andExpect(flash().attribute("mensajeBoleto", "Añadido al boleto"));
        mvc.perform(get("/boleto/anadir").session(sesion).with(COMO_ANA)
                .param("evento", basket.getId().toString()).param("resultado", "LOCAL"));

        mvc.perform(get("/boleto").session(sesion).with(COMO_ANA))
                .andExpect(content().string(containsString("1X · Local o empate")));
        mvc.perform(post("/boleto/confirmar").session(sesion).with(COMO_ANA).with(csrf()).param("importe", "5"))
                .andExpect(redirectedUrl("/apuestas"));

        Apuesta combinada = apuestas.findAll().stream().filter(a -> a.getUsuario().getEmail().equals(ANA))
                .findFirst().orElseThrow();
        assertThat(combinada.isCombinada()).isTrue();
        assertThat(combinada.getSelecciones()).extracting(Seleccion::getSimbolo).containsExactly("1X", "1");
    }

    @Test
    void enBaloncestoNoSePuedenHacer() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(get("/boleto/anadir").session(sesion).with(COMO_ANA)
                        .param("evento", basket.getId().toString()).param("especial", "MAS_2_5"))
                .andExpect(flash().attribute("errorBoleto", containsString("solo está disponible en los partidos de fútbol")));
        mvc.perform(post("/eventos/{id}/apostar", basket.getId()).with(COMO_ANA).with(csrf())
                        .param("opcion", "AMBOS_SI").param("importe", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("solo está disponible en los partidos de fútbol")));
        assertThat(apuestas.findAll()).noneMatch(a -> a.getUsuario().getEmail().equals(ANA));
    }

    @Test
    void sinPronosticoSePideElegirUno() throws Exception {
        mvc.perform(post("/eventos/{id}/apostar", futbol.getId()).with(COMO_ANA).with(csrf()).param("importe", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Elige un pronóstico")));
    }

    @Test
    void enLasApuestasSeVeElTipo() throws Exception {
        apostar(futbol, "MAS_2_5");
        mvc.perform(get("/apuestas").with(COMO_ANA))
                .andExpect(content().string(containsString("Más de 2,5 goles")));
    }
}

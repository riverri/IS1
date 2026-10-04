package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-04, HU-05, HU-06, HU-25, HU-34, HU-35 y HU-36: resolución de apuestas y lo que depende de ella. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResolucionWebTest {

    private static final RequestPostProcessor CREADOR = user("creador@apuestas.es").roles("CREADOR");
    private static final String ANA = "ana@ucm.es";
    private static final String LUIS = "luis@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApuestaService apuestaService;

    @Autowired
    private ApuestaRepository apuestas;

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

    private Evento futbol;
    private Apuesta deAna;
    private Apuesta deLuis;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        futbol = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusHours(2)));
        deAna = apuestaService.apostar(ANA, futbol.getId(), Resultado.LOCAL, new BigDecimal("100"));
        deLuis = apuestaService.apostar(LUIS, futbol.getId(), Resultado.VISITANTE, new BigDecimal("50"));
    }

    private BigDecimal saldo(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo();
    }

    @Test
    void introducirResultadoResuelveYPagaLasApuestas() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                        .param("resultado", "LOCAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensaje"));

        assertThat(eventos.findById(futbol.getId()).orElseThrow().getEstado()).isEqualTo(EstadoEvento.FINALIZADO);
        assertThat(deAna.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        assertThat(deLuis.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
        assertThat(saldo(ANA)).isEqualByComparingTo(new BigDecimal("900").add(deAna.getGananciaPotencial()));
        assertThat(saldo(LUIS)).isEqualByComparingTo("950");
    }

    @Test
    void corregirElResultadoReajustaLosSaldos() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                .param("resultado", "LOCAL"));
        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                .param("resultado", "VISITANTE"));

        assertThat(deAna.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
        assertThat(deLuis.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        assertThat(saldo(ANA)).isEqualByComparingTo("900");
        assertThat(saldo(LUIS)).isEqualByComparingTo(new BigDecimal("950").add(deLuis.getGananciaPotencial()));
    }

    @Test
    void elFinalizadoYaNoAdmiteApuestas() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                .param("resultado", "EMPATE"));

        mvc.perform(post("/eventos/{id}/apostar", futbol.getId()).with(user(ANA)).with(csrf())
                        .param("resultado", "LOCAL").param("importe", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void noHayEmpateEnBaloncesto() throws Exception {
        Evento basket = eventos.save(new Evento(competiciones.findByNombre("Liga ACB").orElseThrow(),
                equipos.findByNombre("Unicaja").orElseThrow(),
                equipos.findByNombre("Baskonia").orElseThrow(), Hora.ahora().minusHours(2)));

        mvc.perform(post("/gestion/eventos/{id}/resultado", basket.getId()).with(CREADOR).with(csrf())
                        .param("resultado", "EMPATE"))
                .andExpect(flash().attributeExists("error"));

        assertThat(basket.getEstado()).isEqualTo(EstadoEvento.PROGRAMADO);
    }

    @Test
    void suspenderQuitaElEventoDelCatalogoYReactivarLoDevuelve() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/suspender", futbol.getId()).with(CREADOR).with(csrf()));
        mvc.perform(get("/eventos")).andExpect(content().string(not(containsString("Getafe CF – Sevilla FC"))));
        mvc.perform(post("/eventos/{id}/apostar", futbol.getId()).with(user(ANA)).with(csrf())
                        .param("resultado", "LOCAL").param("importe", "10"))
                .andExpect(status().isNotFound());

        mvc.perform(post("/gestion/eventos/{id}/reactivar", futbol.getId()).with(CREADOR).with(csrf()));
        mvc.perform(get("/eventos")).andExpect(content().string(containsString("Getafe CF – Sevilla FC")));
    }

    @Test
    void anularDevuelveElImporteATodos() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/anular", futbol.getId()).with(CREADOR).with(csrf()));

        assertThat(eventos.findById(futbol.getId()).orElseThrow().getEstado()).isEqualTo(EstadoEvento.ANULADO);
        assertThat(deAna.getEstado()).isEqualTo(EstadoApuesta.ANULADA);
        assertThat(saldo(ANA)).isEqualByComparingTo("1000");
        assertThat(saldo(LUIS)).isEqualByComparingTo("1000");
    }

    @Test
    void elCreadorVeCuantoSeHaApostadoACadaResultado() throws Exception {
        mvc.perform(get("/gestion/eventos/{id}", futbol.getId()).with(CREADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("100,00")))
                .andExpect(content().string(containsString("50,00")));
    }

    @Test
    void unUsuarioNoPuedeIntroducirResultados() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(user(ANA)).with(csrf())
                        .param("resultado", "LOCAL"))
                .andExpect(status().isForbidden());
        assertThat(deAna.getEstado()).isEqualTo(EstadoApuesta.ACTIVA);
    }

    @Test
    void historialYEstadisticasTrasResolver() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                .param("resultado", "LOCAL"));

        mvc.perform(get("/apuestas").with(user(ANA)))
                .andExpect(content().string(containsString("GANADA")))
                .andExpect(content().string(containsString("100,0 %")));
        mvc.perform(get("/apuestas").with(user(LUIS)))
                .andExpect(content().string(containsString("PERDIDA")))
                .andExpect(content().string(containsString("-50,00")));
    }

    @Test
    void rankingPorGananciasYPorAciertos() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                .param("resultado", "LOCAL"));

        mvc.perform(get("/ranking").param("orden", "GANANCIAS"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesRegex("(?s).*Ana.*Luis.*")));
        mvc.perform(get("/ranking").param("orden", "ACIERTOS"))
                .andExpect(content().string(matchesRegex("(?s).*Ana.*100,0 %.*Luis.*0,0 %.*")));
    }
}

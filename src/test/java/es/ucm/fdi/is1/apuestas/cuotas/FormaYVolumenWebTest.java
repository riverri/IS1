package es.ucm.fdi.is1.apuestas.cuotas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

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

import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.Forma;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-02 y fila 3: la forma reciente y el dinero apostado mueven las cuotas de un partido real. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FormaYVolumenWebTest {

    private static final RequestPostProcessor CREADOR = user("creador@apuestas.es").roles("CREADOR");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CalculadoraCuotas calculadora;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento partido;
    private Equipo getafe;

    @BeforeEach
    void crearPartido() {
        getafe = equipos.findByNombre("Getafe CF").orElseThrow();
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(), getafe,
                equipos.findByNombre("Sevilla FC").orElseThrow(), LocalDateTime.now().plusDays(1)));
    }

    @Test
    void elCreadorCambiaLaFormaYBajaLaCuota() throws Exception {
        BigDecimal antes = calculadora.calcular(partido).local();

        mvc.perform(post("/gestion/equipos/{id}/editar", getafe.getId()).with(CREADOR).with(csrf())
                        .param("calidad", getafe.getCalidad().toString())
                        .param("forma", "MUY_BUENA"))
                .andExpect(redirectedUrl("/gestion"));

        assertThat(getafe.getForma()).isEqualTo(Forma.MUY_BUENA);
        assertThat(calculadora.calcular(partido).local()).isLessThan(antes);
        mvc.perform(get("/equipos"))
                .andExpect(content().string(containsString("Muy buena racha")));
    }

    @Test
    void laPaginaDeEdicionMuestraLaFormaActual() throws Exception {
        getafe.setForma(Forma.MALA);

        mvc.perform(get("/gestion/equipos/{id}/editar", getafe.getId()).with(CREADOR))
                .andExpect(content().string(containsString("value=\"MALA\" selected")));
    }

    @Test
    void elDineroApostadoBajaLaCuotaDeEseResultado() {
        Cuotas antes = calculadora.calcular(partido);
        usuarios.crear("ana@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        usuarios.crear("luis@ucm.es", "Luis", "secreta123", Rol.USUARIO);

        apuestas.apostar("ana@ucm.es", partido.getId(), Resultado.VISITANTE, new BigDecimal("500"));
        apuestas.apostar("luis@ucm.es", partido.getId(), Resultado.VISITANTE, new BigDecimal("500"));

        Cuotas despues = calculadora.calcular(partido);
        assertThat(despues.visitante()).isLessThan(antes.visitante());
        assertThat(despues.local()).isGreaterThan(antes.local());
    }

    @Test
    void lasApuestasYaHechasConservanSuCuota() {
        usuarios.crear("ana@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        BigDecimal cuota = calculadora.calcular(partido).visitante();

        var apuesta = apuestas.apostar("ana@ucm.es", partido.getId(), Resultado.VISITANTE, new BigDecimal("500"));

        assertThat(apuesta.getCuota()).isEqualByComparingTo(cuota);
        assertThat(calculadora.calcular(partido).visitante()).isNotEqualByComparingTo(cuota);
    }
}

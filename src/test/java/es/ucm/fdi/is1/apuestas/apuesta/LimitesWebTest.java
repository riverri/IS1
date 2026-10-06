package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/** HU-07: límites de apuesta que fija el creador. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LimitesWebTest {

    private static final RequestPostProcessor CREADOR = user("creador@apuestas.es").roles("CREADOR");
    private static final RequestPostProcessor USUARIO = user("usuario@apuestas.es").roles("USUARIO");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private LimitesService limites;

    @Autowired
    private ApuestaRepository apuestas;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento partido;

    @BeforeEach
    void crearPartido() {
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
    }

    private void cambiarLimites(String minimo, String maximo, String selecciones) throws Exception {
        mvc.perform(post("/gestion/limites").with(CREADOR).with(csrf())
                        .param("importeMinimo", minimo)
                        .param("importeMaximo", maximo)
                        .param("maxSelecciones", selecciones))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestion"));
    }

    @Test
    void porDefectoEntre1y500MonedasYHasta10Selecciones() {
        Limites actuales = limites.actuales();
        assertThat(actuales.getImporteMinimo()).isEqualByComparingTo("1");
        assertThat(actuales.getImporteMaximo()).isEqualByComparingTo("500");
        assertThat(actuales.getMaxSelecciones()).isEqualTo(10);
    }

    @Test
    void elCreadorCambiaLosLimites() throws Exception {
        cambiarLimites("2", "50", "4");

        Limites actuales = limites.actuales();
        assertThat(actuales.getImporteMinimo()).isEqualByComparingTo("2");
        assertThat(actuales.getImporteMaximo()).isEqualByComparingTo("50");
        assertThat(actuales.getMaxSelecciones()).isEqualTo(4);
        mvc.perform(get("/gestion").with(CREADOR))
                .andExpect(content().string(containsString("Importe entre 2,00 y 50,00 monedas")));
    }

    @Test
    void elMaximoNoPuedeSerMenorQueElMinimo() throws Exception {
        mvc.perform(post("/gestion/limites").with(CREADOR).with(csrf())
                        .param("importeMinimo", "20")
                        .param("importeMaximo", "10")
                        .param("maxSelecciones", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no puede ser menor que el mínimo")));
        assertThat(limites.actuales().getImporteMaximo()).isEqualByComparingTo("500");
    }

    @Test
    void unUsuarioNormalNoCambiaLosLimites() throws Exception {
        mvc.perform(post("/gestion/limites").with(USUARIO).with(csrf())
                        .param("importeMinimo", "1").param("importeMaximo", "100000").param("maxSelecciones", "30"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rechazaApuestasFueraDeLosLimites() throws Exception {
        cambiarLimites("5", "50", "10");

        for (String importe : new String[] {"4.99", "50.01"}) {
            mvc.perform(post("/eventos/{id}/apostar", partido.getId()).with(USUARIO).with(csrf())
                            .param("resultado", "LOCAL").param("importe", importe))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("El importe debe estar entre 5 y 50 monedas")));
        }
        assertThat(apuestas.count()).isZero();

        mvc.perform(post("/eventos/{id}/apostar", partido.getId()).with(USUARIO).with(csrf())
                        .param("resultado", "LOCAL").param("importe", "50"))
                .andExpect(redirectedUrl("/apuestas"));
        assertThat(apuestas.count()).isEqualTo(1);
    }

    @Test
    void elBoletoRespetaElMaximoDeSelecciones() throws Exception {
        cambiarLimites("1", "500", "2");
        MockHttpSession sesion = new MockHttpSession();
        Evento otro = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(),
                equipos.findByNombre("Levante UD").orElseThrow(), Hora.ahora().plusDays(2)));
        Evento tercero = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Málaga CF").orElseThrow(),
                equipos.findByNombre("Celta de Vigo").orElseThrow(), Hora.ahora().plusDays(2)));

        for (Evento evento : new Evento[] {partido, otro}) {
            mvc.perform(post("/boleto/anadir").session(sesion).with(USUARIO).with(csrf())
                    .param("evento", evento.getId().toString()).param("resultado", "LOCAL"));
        }
        mvc.perform(post("/boleto/anadir").session(sesion).with(USUARIO).with(csrf())
                        .param("evento", tercero.getId().toString()).param("resultado", "LOCAL"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash()
                        .attribute("errorBoleto", containsString("como máximo 2 selecciones")));
    }
}

package es.ucm.fdi.is1.apuestas.gestion;

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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/** HU-46: el creador edita o borra un evento que ha creado mal. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EditarEventoWebTest {

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
    private ApuestaService apuestas;

    private Evento evento;
    private Equipo getafe;
    private Equipo sevilla;
    private Equipo elche;
    private LocalDateTime nuevaFecha;

    @BeforeEach
    void crearEvento() {
        getafe = equipos.findByNombre("Getafe CF").orElseThrow();
        sevilla = equipos.findByNombre("Sevilla FC").orElseThrow();
        elche = equipos.findByNombre("Elche CF").orElseThrow();
        evento = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(), getafe, sevilla,
                LocalDateTime.now().plusDays(2)));
        nuevaFecha = LocalDateTime.now().plusDays(5).truncatedTo(ChronoUnit.MINUTES);
    }

    private org.springframework.test.web.servlet.ResultActions editar(Equipo local, Equipo visitante) throws Exception {
        return mvc.perform(post("/gestion/eventos/{id}/editar", evento.getId()).with(CREADOR).with(csrf())
                .param("localId", local.getId().toString())
                .param("visitanteId", visitante.getId().toString())
                .param("fechaHora", nuevaFecha.toString())
                .param("fase", "Jornada 12"));
    }

    @Test
    void editaEquiposFechaYFase() throws Exception {
        editar(elche, sevilla).andExpect(redirectedUrl("/gestion/eventos/" + evento.getId()));

        assertThat(evento.getLocal()).isEqualTo(elche);
        assertThat(evento.getFechaHora()).isEqualTo(nuevaFecha);
        assertThat(evento.getFase()).isEqualTo("Jornada 12");
    }

    @Test
    void laPaginaDeEdicionMuestraLosDatosActuales() throws Exception {
        mvc.perform(get("/gestion/eventos/{id}/editar", evento.getId()).with(CREADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Editar evento")))
                .andExpect(content().string(containsString("value=\"" + getafe.getId() + "\" selected")));
    }

    @Test
    void conApuestasNoSeCambianLosEquiposPeroSiLaFecha() throws Exception {
        apuestas.apostar("usuario@apuestas.es", evento.getId(), Resultado.LOCAL, BigDecimal.TEN);

        editar(elche, sevilla)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no se pueden cambiar los equipos")));
        assertThat(evento.getLocal()).isEqualTo(getafe);

        editar(getafe, sevilla).andExpect(redirectedUrl("/gestion/eventos/" + evento.getId()));
        assertThat(evento.getFechaHora()).isEqualTo(nuevaFecha);
    }

    @Test
    void rechazaElMismoEquipoYEquiposDeOtraCompeticion() throws Exception {
        editar(sevilla, sevilla).andExpect(content().string(containsString("deben ser distintos")));
        Equipo arsenal = equipos.findByNombre("Arsenal").orElseThrow();
        editar(arsenal, sevilla).andExpect(content().string(containsString("no participa en LaLiga")));
        assertThat(evento.getLocal()).isEqualTo(getafe);
    }

    @Test
    void unEventoEmpezadoNoSeEdita() throws Exception {
        Evento empezado = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(), elche, getafe,
                LocalDateTime.now().minusMinutes(10)));

        mvc.perform(post("/gestion/eventos/{id}/editar", empezado.getId()).with(CREADOR).with(csrf())
                        .param("localId", elche.getId().toString())
                        .param("visitanteId", getafe.getId().toString())
                        .param("fechaHora", nuevaFecha.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no se puede editar")));
        assertThat(empezado.getFechaHora()).isBefore(LocalDateTime.now());
    }

    @Test
    void borraUnEventoSinApuestas() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/borrar", evento.getId()).with(CREADOR).with(csrf()))
                .andExpect(redirectedUrl("/gestion"));

        assertThat(eventos.findById(evento.getId())).isEmpty();
    }

    @Test
    void conApuestasNoSeBorra() throws Exception {
        apuestas.apostar("usuario@apuestas.es", evento.getId(), Resultado.LOCAL, BigDecimal.TEN);

        mvc.perform(post("/gestion/eventos/{id}/borrar", evento.getId()).with(CREADOR).with(csrf()))
                .andExpect(flash().attribute("error", containsString("solo anular")));
        assertThat(eventos.findById(evento.getId())).isPresent();
    }

    @Test
    void unUsuarioNormalNoEditaNiBorra() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/borrar", evento.getId()).with(user("usuario@apuestas.es")).with(csrf()))
                .andExpect(status().isForbidden());
    }
}

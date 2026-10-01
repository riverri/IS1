package es.ucm.fdi.is1.apuestas.eventos;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;

/** HU-08 y HU-19: catálogo público de eventos. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogoWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento eventoFuturo;

    private Competicion competicion(String nombre) {
        return competiciones.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Equipo equipo(String nombre) {
        return equipos.findAll().stream().filter(e -> e.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    @BeforeEach
    void crearEventos() {
        LocalDateTime ahora = LocalDateTime.now();
        eventoFuturo = eventos.save(new Evento(competicion("LaLiga"),
                equipo("Real Madrid"), equipo("Getafe CF"), ahora.plusDays(1)));
        eventos.save(new Evento(competicion("Liga ACB"),
                equipo("Unicaja"), equipo("Barça Basket"), ahora.plusDays(2)));
        eventos.save(new Evento(competicion("LaLiga"),
                equipo("FC Barcelona"), equipo("Getafe CF"), ahora.minusDays(3)));
    }

    @Test
    void unVisitanteVeElCatalogoAgrupadoPorDeporte() throws Exception {
        mvc.perform(get("/eventos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fútbol")))
                .andExpect(content().string(containsString("Baloncesto")))
                .andExpect(content().string(containsString("Real Madrid – Getafe CF")))
                .andExpect(content().string(containsString("Unicaja – Barça Basket")));
    }

    @Test
    void losEventosYaJugadosNoAparecen() throws Exception {
        mvc.perform(get("/eventos"))
                .andExpect(content().string(not(containsString("FC Barcelona – Getafe CF"))));
    }

    @Test
    void unVisitanteQueIntentaApostarVaAlLogin() throws Exception {
        mvc.perform(get("/eventos/{id}/apostar", eventoFuturo.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "usuario@apuestas.es")
    void unUsuarioConSesionPuedeEntrarAApostar() throws Exception {
        mvc.perform(get("/eventos/{id}/apostar", eventoFuturo.getId()))
                .andExpect(status().isOk());
    }
}

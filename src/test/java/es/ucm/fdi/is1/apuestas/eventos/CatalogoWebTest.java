package es.ucm.fdi.is1.apuestas.eventos;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** HU-08 y HU-19: catálogo público de eventos. */
@SpringBootTest
@AutoConfigureMockMvc
class CatalogoWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EventoRepository eventos;

    private Long idEventoFuturo() {
        return eventos.findAllByOrderByFechaHoraAsc().stream()
                .filter(e -> e.getEstado() == EstadoEvento.PROGRAMADO
                        && e.getFechaHora().isAfter(java.time.LocalDateTime.now()))
                .findFirst().orElseThrow().getId();
    }

    @Test
    void unVisitanteVeElCatalogoAgrupadoPorDeporte() throws Exception {
        mvc.perform(get("/eventos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fútbol")))
                .andExpect(content().string(containsString("Baloncesto")))
                .andExpect(content().string(containsString("Real Madrid – Getafe CF")));
    }

    @Test
    void losEventosYaJugadosNoAparecen() throws Exception {
        mvc.perform(get("/eventos"))
                .andExpect(content().string(not(containsString("FC Barcelona – Getafe CF"))));
    }

    @Test
    void unVisitanteQueIntentaApostarVaAlLogin() throws Exception {
        mvc.perform(get("/eventos/{id}/apostar", idEventoFuturo()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "usuario@apuestas.es")
    void unUsuarioConSesionPuedeEntrarAApostar() throws Exception {
        mvc.perform(get("/eventos/{id}/apostar", idEventoFuturo()))
                .andExpect(status().isOk());
    }
}

package es.ucm.fdi.is1.apuestas.equipos;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Listado de equipos por deporte y competición, con buscador. */
@SpringBootTest
@AutoConfigureMockMvc
class EquiposWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CompeticionRepository competiciones;

    @Test
    void agrupaPorDeporte() throws Exception {
        mvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Baloncesto")))
                .andExpect(content().string(containsString("deporte=TENIS")));
    }

    @Test
    void filtraPorDeporte() throws Exception {
        mvc.perform(get("/equipos").param("deporte", "TENIS"))
                .andExpect(content().string(containsString("Carlos Alcaraz")))
                .andExpect(content().string(not(containsString("Getafe CF"))));
    }

    @Test
    void filtraPorCompeticion() throws Exception {
        Long champions = competiciones.findByNombre("Champions League").orElseThrow().getId();
        mvc.perform(get("/equipos").param("deporte", "FUTBOL").param("competicion", champions.toString()))
                .andExpect(content().string(containsString("Arsenal")))
                .andExpect(content().string(not(containsString("Getafe CF"))));
    }

    @Test
    void buscaSinTildes() throws Exception {
        mvc.perform(get("/equipos").param("q", "malaga"))
                .andExpect(content().string(containsString("Málaga CF")))
                .andExpect(content().string(not(containsString("Getafe CF"))));
    }
}

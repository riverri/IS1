package es.ucm.fdi.is1.apuestas.eventos;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;

/** HU-22: búsqueda y filtros del catálogo por deporte y texto. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BusquedaWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @BeforeEach
    void crearEventos() {
        LocalDateTime manana = Hora.ahora().plusDays(1);
        eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Atlético de Madrid").orElseThrow(),
                equipos.findByNombre("Málaga CF").orElseThrow(), manana));
        eventos.save(new Evento(competiciones.findByNombre("ATP Masters 1000").orElseThrow(),
                equipos.findByNombre("Carlos Alcaraz").orElseThrow(),
                equipos.findByNombre("Jannik Sinner").orElseThrow(), manana));
    }

    @Test
    void filtraPorDeporte() throws Exception {
        mvc.perform(get("/eventos").param("deporte", "TENIS"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Carlos Alcaraz – Jannik Sinner")))
                .andExpect(content().string(not(containsString("Atlético de Madrid – Málaga CF"))));
    }

    @Test
    void buscaSinDistinguirTildesNiMayusculas() throws Exception {
        mvc.perform(get("/eventos").param("q", "ATLETICO"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Atlético de Madrid – Málaga CF")))
                .andExpect(content().string(not(containsString("Carlos Alcaraz – Jannik Sinner"))));
    }

    @Test
    void buscaPorCompeticion() throws Exception {
        mvc.perform(get("/eventos").param("q", "masters"))
                .andExpect(content().string(containsString("Carlos Alcaraz – Jannik Sinner")));
    }

    @Test
    void avisaSiNoHayResultados() throws Exception {
        mvc.perform(get("/eventos").param("q", "equipo que no existe"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ningún evento coincide")));
    }

    @Test
    void muestraLasPestanasDeDeportes() throws Exception {
        mvc.perform(get("/eventos"))
                .andExpect(content().string(containsString("deporte=TENIS")))
                .andExpect(content().string(containsString("deporte=FUTBOL")));
    }
}

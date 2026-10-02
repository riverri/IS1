package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-48: perfil público de un jugador desde el ranking, con sus cifras y sin sus apuestas. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerfilJugadorWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Usuario ana;

    @BeforeEach
    void preparar() {
        ana = usuarioService.crear("ana@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        Evento jugado = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), LocalDateTime.now().plusDays(1)));
        Evento pendiente = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(),
                equipos.findByNombre("Levante UD").orElseThrow(), LocalDateTime.now().plusDays(2)));
        apuestas.apostar("ana@ucm.es", jugado.getId(), Resultado.LOCAL, new BigDecimal("10"));
        apuestas.apostar("ana@ucm.es", pendiente.getId(), Resultado.LOCAL, new BigDecimal("10"));
        resolucion.introducirResultado(jugado.getId(), Resultado.LOCAL);
    }

    @Test
    void elRankingEnlazaConLosPerfiles() throws Exception {
        mvc.perform(get("/ranking"))
                .andExpect(content().string(containsString("href=\"/jugadores/" + ana.getId() + "\"")));
    }

    @Test
    void elPerfilEsPublicoYMuestraSusCifrasPeroNoSusApuestas() throws Exception {
        mvc.perform(get("/jugadores/{id}", ana.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana")))
                .andExpect(content().string(containsString("100,0 %")))
                .andExpect(content().string(containsString("apuestas en juego")))
                .andExpect(content().string(not(containsString("Getafe CF"))))
                .andExpect(content().string(not(containsString("ana@ucm.es"))));
    }

    @Autowired
    private RankingService rankings;

    @Test
    void cuentaLasApuestasEnJuego() {
        PerfilJugador perfil = rankings.perfil(ana.getId(), null);

        assertThat(perfil.apuestasEnJuego()).isEqualTo(1);
        assertThat(perfil.puesto().estadisticas().resueltas()).isEqualTo(1);
    }

    @Test
    void elCreadorYLosQueNoExistenNoTienenPerfil() throws Exception {
        Usuario creador = usuarios.findByEmail("creador@apuestas.es").orElseThrow();
        mvc.perform(get("/jugadores/{id}", creador.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/jugadores/{id}", 999999)).andExpect(status().isNotFound());
    }
}

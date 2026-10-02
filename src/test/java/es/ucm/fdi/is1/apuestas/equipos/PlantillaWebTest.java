package es.ucm.fdi.is1.apuestas.equipos;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

/** Plantilla y alineación probable en la ficha del equipo, y gestión de jugadores por el creador. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlantillaWebTest {

    private static final RequestPostProcessor CREADOR = user("creador@apuestas.es").roles("CREADOR");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private JugadorRepository jugadores;

    @Autowired
    private PlantillaService plantillas;

    private Equipo getafe;

    @BeforeEach
    void preparar() {
        getafe = equipos.findByNombre("Getafe CF").orElseThrow();
    }

    private void onceCompleto() {
        plantillas.anadir(getafe.getId(), "Portero Titular", Posicion.PORTERO, 1, 7.0, "España");
        for (int i = 2; i <= 5; i++) {
            plantillas.anadir(getafe.getId(), "Defensa " + i, Posicion.DEFENSA, i, 6.5, null);
        }
        for (int i = 6; i <= 8; i++) {
            plantillas.anadir(getafe.getId(), "Medio " + i, Posicion.CENTROCAMPISTA, i, 6.5, null);
        }
        for (int i = 9; i <= 11; i++) {
            plantillas.anadir(getafe.getId(), "Delantero " + i, Posicion.DELANTERO, i, 8.2, null);
        }
    }

    @Test
    void sinJugadoresLaFichaLoExplica() throws Exception {
        mvc.perform(get("/equipos/{id}", getafe.getId()))
                .andExpect(content().string(containsString("Todavía no hay jugadores de este equipo")));
    }

    @Test
    void laFichaMuestraLaPlantillaYLaAlineacionProbable() throws Exception {
        onceCompleto();

        mvc.perform(get("/equipos/{id}", getafe.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alineación probable")))
                .andExpect(content().string(containsString("4-3-3")))
                .andExpect(content().string(containsString("Portero Titular")))
                .andExpect(content().string(containsString("Delanteros")))
                .andExpect(content().string(containsString("8,2")));
    }

    @Test
    void conUnaPlantillaIncompletaHayPlantillaPeroNoAlineacion() throws Exception {
        plantillas.anadir(getafe.getId(), "Solo Uno", Posicion.DELANTERO, 9, 7.0, null);

        mvc.perform(get("/equipos/{id}", getafe.getId()))
                .andExpect(content().string(containsString("Solo Uno")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Alineación probable"))));
    }

    @Test
    void elCreadorAnadePonNotaYQuitaJugadores() throws Exception {
        mvc.perform(post("/gestion/equipos/{id}/jugadores", getafe.getId()).with(CREADOR).with(csrf())
                        .param("nombre", "Nuevo Fichaje").param("posicion", "CENTROCAMPISTA")
                        .param("dorsal", "21").param("nota", "7.5").param("nacionalidad", "Argentina"))
                .andExpect(redirectedUrl("/gestion/equipos/" + getafe.getId() + "/editar#jugadores"));
        Jugador fichaje = jugadores.findByEquipo(getafe).get(0);
        assertThat(fichaje.getNombre()).isEqualTo("Nuevo Fichaje");
        assertThat(fichaje.getNota()).isEqualTo(7.5);

        mvc.perform(get("/gestion/equipos/{id}/editar", getafe.getId()).with(CREADOR))
                .andExpect(content().string(containsString("Nuevo Fichaje")));

        mvc.perform(post("/gestion/jugadores/{id}/nota", fichaje.getId()).with(CREADOR).with(csrf()).param("nota", "9.1"))
                .andExpect(flash().attribute("mensaje", containsString("9,1")));
        assertThat(fichaje.getNota()).isEqualTo(9.1);

        mvc.perform(post("/gestion/jugadores/{id}/borrar", fichaje.getId()).with(CREADOR).with(csrf()))
                .andExpect(redirectedUrl("/gestion/equipos/" + getafe.getId() + "/editar#jugadores"));
        assertThat(jugadores.findByEquipo(getafe)).isEmpty();
    }

    @Test
    void rechazaDatosNoValidos() throws Exception {
        mvc.perform(post("/gestion/equipos/{id}/jugadores", getafe.getId()).with(CREADOR).with(csrf())
                        .param("nombre", "Dorsal Raro").param("posicion", "DEFENSA").param("dorsal", "150"))
                .andExpect(flash().attribute("errorJugadores", containsString("entre 1 y 99")));
        mvc.perform(post("/gestion/equipos/{id}/jugadores", getafe.getId()).with(CREADOR).with(csrf())
                        .param("nombre", "Nota Rara").param("posicion", "DEFENSA").param("nota", "11"))
                .andExpect(flash().attribute("errorJugadores", containsString("entre 0 y 10")));
        assertThat(jugadores.findByEquipo(getafe)).isEmpty();
    }

    @Test
    void unUsuarioNormalNoGestionaJugadores() throws Exception {
        mvc.perform(post("/gestion/equipos/{id}/jugadores", getafe.getId()).with(user("usuario@apuestas.es"))
                        .with(csrf()).param("nombre", "Intruso").param("posicion", "DEFENSA"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinClaveNoSeDescarganPlantillas() throws Exception {
        mvc.perform(post("/gestion/sincronizar-plantillas").with(CREADOR).with(csrf()))
                .andExpect(flash().attribute("error", containsString("FOOTBALL_DATA_TOKEN")));
    }

    @Test
    void unVisitanteVeLaFichaSinQueSeLeCreeSesion() throws Exception {
        onceCompleto();

        var resultado = mvc.perform(get("/equipos/{id}", getafe.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("</html>")))
                .andReturn();

        // El boleto vive en la sesión: si se creara a mitad de página, la respuesta se cortaría
        assertThat(resultado.getRequest().getSession(false)).isNull();
    }
}

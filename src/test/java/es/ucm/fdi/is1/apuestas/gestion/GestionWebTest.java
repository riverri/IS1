package es.ucm.fdi.is1.apuestas.gestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

/** HU-01 y HU-21: alta de competiciones, equipos y eventos por el creador de apuestas. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GestionWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private CompeticionRepository competiciones;

    private Competicion competicion(String nombre) {
        return competiciones.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Equipo equipo(String nombre) {
        return equipos.findAll().stream().filter(e -> e.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    @Test
    @WithMockUser(username = "usuario@apuestas.es", roles = "USUARIO")
    void unUsuarioNormalNoPuedeEntrarEnGestion() throws Exception {
        mvc.perform(get("/gestion")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void elCreadorVeElPanel() throws Exception {
        mvc.perform(get("/gestion"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Panel del creador de apuestas")));
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void altaDeEquipoLoDejaDisponible() throws Exception {
        mvc.perform(post("/gestion/equipos/nuevo").with(csrf())
                        .param("nombre", "Rayo Vallecano")
                        .param("deporte", "FUTBOL")
                        .param("calidad", "6.5")
                        .param("competicionIds", competicion("LaLiga").getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestion"));

        Equipo rayo = equipo("Rayo Vallecano");
        assertThat(rayo.participaEn(competicion("LaLiga").getId())).isTrue();
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void rechazaCalificacionFueraDeRango() throws Exception {
        mvc.perform(post("/gestion/equipos/nuevo").with(csrf())
                        .param("nombre", "Equipo Imposible")
                        .param("deporte", "FUTBOL")
                        .param("calidad", "11"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La calificación debe estar entre 0 y 10")));

        assertThat(equipos.findAll()).noneMatch(e -> e.getNombre().equals("Equipo Imposible"));
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void rechazaCompeticionDeOtroDeporte() throws Exception {
        mvc.perform(post("/gestion/equipos/nuevo").with(csrf())
                        .param("nombre", "Estudiantes")
                        .param("deporte", "BALONCESTO")
                        .param("calidad", "6")
                        .param("competicionIds", competicion("LaLiga").getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no es de Baloncesto")));
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void altaDeEvento() throws Exception {
        mvc.perform(post("/gestion/eventos/nuevo").with(csrf())
                        .param("competicionId", competicion("LaLiga").getId().toString())
                        .param("localId", equipo("Getafe CF").getId().toString())
                        .param("visitanteId", equipo("Atlético de Madrid").getId().toString())
                        .param("fechaHora", "2099-05-10T18:30"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/eventos"))
                .andExpect(content().string(containsString("Getafe CF – Atlético de Madrid")));
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void eventoRechazaEquipoQueNoParticipaEnLaCompeticion() throws Exception {
        mvc.perform(post("/gestion/eventos/nuevo").with(csrf())
                        .param("competicionId", competicion("Champions League").getId().toString())
                        .param("localId", equipo("Getafe CF").getId().toString())
                        .param("visitanteId", equipo("Real Madrid").getId().toString())
                        .param("fechaHora", "2099-05-10T18:30"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Getafe CF no participa en Champions League")));
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void eventoRechazaMismoEquipoLocalYVisitante() throws Exception {
        String madrid = equipo("Real Madrid").getId().toString();
        mvc.perform(post("/gestion/eventos/nuevo").with(csrf())
                        .param("competicionId", competicion("LaLiga").getId().toString())
                        .param("localId", madrid)
                        .param("visitanteId", madrid)
                        .param("fechaHora", "2099-05-10T18:30"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El local y el visitante deben ser distintos")));
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void cambiaElEscudoDeUnEquipo() throws Exception {
        Equipo getafe = equipo("Getafe CF");
        mvc.perform(post("/gestion/equipos/{id}/escudo", getafe.getId()).with(csrf())
                        .param("escudoUrl", "https://example.org/getafe.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestion"));

        assertThat(equipo("Getafe CF").getEscudoUrl()).isEqualTo("https://example.org/getafe.png");
        mvc.perform(get("/equipos"))
                .andExpect(content().string(containsString("https://example.org/getafe.png")));
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void rechazaEscudoQueNoEsUnaDireccionWeb() throws Exception {
        Equipo getafe = equipo("Getafe CF");
        String antes = getafe.getEscudoUrl();
        mvc.perform(post("/gestion/equipos/{id}/escudo", getafe.getId()).with(csrf())
                        .param("escudoUrl", "javascript:alert(1)"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Debe ser una dirección")));

        assertThat(equipo("Getafe CF").getEscudoUrl()).isEqualTo(antes);
    }

    @Test
    @WithMockUser(username = "creador@apuestas.es", roles = "CREADOR")
    void vaciarElEscudoVuelveALasIniciales() throws Exception {
        Equipo getafe = equipo("Getafe CF");
        mvc.perform(post("/gestion/equipos/{id}/escudo", getafe.getId()).with(csrf()).param("escudoUrl", ""))
                .andExpect(status().is3xxRedirection());

        assertThat(equipo("Getafe CF").getEscudoUrl()).isNull();
    }
}

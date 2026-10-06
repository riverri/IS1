package es.ucm.fdi.is1.apuestas.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;

/** HU-31 y HU-33: ficha de equipo con sus resultados y cara a cara de los rivales de un partido. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FichaEquipoWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FichaEquipoService fichas;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Competicion laLiga;
    private Equipo getafe;
    private Equipo sevilla;
    private Equipo elche;

    @BeforeEach
    void jugarPartidos() {
        laLiga = competiciones.findByNombre("LaLiga").orElseThrow();
        getafe = equipos.findByNombre("Getafe CF").orElseThrow();
        sevilla = equipos.findByNombre("Sevilla FC").orElseThrow();
        elche = equipos.findByNombre("Elche CF").orElseThrow();
        LocalDateTime antes = Hora.ahora().minusDays(30);
        jugado(getafe, sevilla, antes, Resultado.LOCAL);              // Getafe gana
        jugado(sevilla, getafe, antes.plusDays(7), Resultado.EMPATE); // empate
        jugado(elche, getafe, antes.plusDays(14), Resultado.LOCAL);   // Getafe pierde
        jugado(getafe, elche, antes.plusDays(21), Resultado.VISITANTE); // Getafe pierde
    }

    private void jugado(Equipo local, Equipo visitante, LocalDateTime fecha, Resultado resultado) {
        Evento evento = new Evento(laLiga, local, visitante, fecha);
        evento.finalizar(resultado);
        eventos.save(evento);
    }

    @Test
    void resultadoDesdeCadaEquipo() {
        Evento evento = new Evento(laLiga, getafe, sevilla, Hora.ahora());
        evento.finalizar(Resultado.VISITANTE);

        assertThat(ResultadoEquipo.de(evento, getafe)).isEqualTo(ResultadoEquipo.DERROTA);
        assertThat(ResultadoEquipo.de(evento, sevilla)).isEqualTo(ResultadoEquipo.VICTORIA);
    }

    @Test
    void laFichaTieneBalanceRachaYClasificacion() {
        FichaEquipo ficha = fichas.ficha(getafe.getId());

        assertThat(ficha.balance()).isEqualTo(new Balance(1, 1, 2));
        assertThat(ficha.balance().puntos()).isEqualTo(4);
        // De más antiguo a más reciente
        assertThat(ficha.racha()).containsExactly(ResultadoEquipo.VICTORIA, ResultadoEquipo.EMPATE,
                ResultadoEquipo.DERROTA, ResultadoEquipo.DERROTA);
        assertThat(ficha.ultimos().get(0).rival().getNombre()).isEqualTo("Elche CF");
        assertThat(ficha.ultimos().get(0).enCasa()).isTrue();
        // Elche 6 puntos, Getafe 4, Sevilla 1
        FichaEquipo.Puesto puesto = ficha.clasificaciones().stream()
                .filter(p -> p.competicion().getNombre().equals("LaLiga")).findFirst().orElseThrow();
        assertThat(puesto.posicion()).isEqualTo(2);
        assertThat(puesto.participantes()).isEqualTo(3);
    }

    @Test
    void laFichaEsPublica() throws Exception {
        mvc.perform(get("/equipos/{id}", getafe.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Getafe CF")))
                .andExpect(content().string(containsString("Últimos resultados")))
                .andExpect(content().string(containsString("2º")))
                .andExpect(content().string(containsString("/equipos/" + elche.getId())));
    }

    @Test
    void unEquipoSinPartidosLoDice() throws Exception {
        Equipo levante = equipos.findByNombre("Levante UD").orElseThrow();
        mvc.perform(get("/equipos/{id}", levante.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no hay resultados")));
    }

    @Test
    void unEquipoQueNoExisteDa404() throws Exception {
        mvc.perform(get("/equipos/{id}", 999999)).andExpect(status().isNotFound());
    }

    @Test
    void laListaDeEquiposEnlazaConLasFichas() throws Exception {
        mvc.perform(get("/equipos"))
                .andExpect(content().string(containsString("href=\"/equipos/" + getafe.getId() + "\"")));
    }

    @Test
    void caraACaraEntreLosRivalesDeUnPartido() throws Exception {
        Evento proximo = eventos.save(new Evento(laLiga, getafe, sevilla, Hora.ahora().plusDays(3)));

        CaraACara cc = fichas.caraACara(proximo);
        assertThat(cc.anteriores()).hasSize(2);
        assertThat(cc.balanceLocal()).isEqualTo(new Balance(1, 1, 0));
        assertThat(cc.rachaLocal()).hasSize(4);

        mvc.perform(get("/eventos/{id}/apostar", proximo.getId()).with(user("usuario@apuestas.es")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cara a cara")))
                .andExpect(content().string(containsString("Sevilla FC – Getafe CF")));
    }

    @Test
    void sinEnfrentamientosPreviosSeIndica() throws Exception {
        // Sevilla y Elche no se han enfrentado en los partidos de la prueba
        Evento proximo = eventos.save(new Evento(laLiga, sevilla, elche, Hora.ahora().plusDays(3)));

        assertThat(fichas.caraACara(proximo).anteriores()).isEmpty();
        mvc.perform(get("/eventos/{id}/apostar", proximo.getId()).with(user("usuario@apuestas.es")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cara a cara")))
                .andExpect(content().string(containsString("No hay enfrentamientos anteriores registrados.")));
    }

    @Test
    void elGraficoDeEvolucionAcumulaLosPuntos() throws Exception {
        GraficoEvolucion grafico = fichas.ficha(getafe.getId()).evolucion();

        // Victoria, empate, derrota, derrota: 3, 4, 4, 4 puntos
        assertThat(grafico.puntos()).hasSize(4);
        assertThat(grafico.puntos()).extracting(GraficoEvolucion.Marca::clase)
                .containsExactly("victoria", "empate", "derrota", "derrota");
        assertThat(grafico.puntos().get(1).texto()).contains("Empate contra Sevilla FC").endsWith("4 puntos");
        assertThat(grafico.puntos().get(0).y()).isGreaterThan(grafico.puntos().get(1).y());
        assertThat(grafico.linea()).startsWith("M").contains(" L");

        mvc.perform(get("/equipos/{id}", getafe.getId()))
                .andExpect(content().string(containsString("Evolución")))
                .andExpect(content().string(containsString("grafico-linea")));
    }

    @Test
    void conMenosDeDosPartidosNoHayGrafico() {
        assertThat(fichas.ficha(equipos.findByNombre("Levante UD").orElseThrow().getId()).evolucion()).isNull();
    }
}

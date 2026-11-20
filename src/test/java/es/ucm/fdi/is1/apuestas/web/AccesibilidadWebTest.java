package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Accesibilidad (HU-97, HU-98 y HU-100): lo que se comprueba en el HTML que genera el servidor. Lo que depende del
 * navegador (orden del tabulador, contraste pintado, ampliar al 200 %) lo comprueba la prueba con axe de la CI.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccesibilidadWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

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

    @Autowired
    private Formato formato;

    private Evento partido(String local, String visitante) {
        return eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre(local).orElseThrow(), equipos.findByNombre(visitante).orElseThrow(),
                Hora.ahora().plusDays(1)));
    }

    /** CA-97.3: el primer enlace de cada página salta el menú y lleva al contenido. */
    @Test
    void cadaPaginaEmpiezaConUnEnlaceParaSaltarAlContenido() throws Exception {
        for (String pagina : new String[] {"/", "/eventos", "/login", "/ranking", "/ligas/clasificacion"}) {
            String html = mvc.perform(get(pagina)).andReturn().getResponse().getContentAsString();
            int saltar = html.indexOf("class=\"saltar-contenido\" href=\"#contenido\"");
            assertThat(saltar).as(pagina).isPositive();
            // Es el primer enlace de la página (antes que el logo y el menú) y su destino existe
            assertThat(html.indexOf("<a ")).as(pagina).isEqualTo(html.lastIndexOf("<a ", saltar));
            assertThat(html).as(pagina).contains("id=\"contenido\" tabindex=\"-1\"");
        }
    }

    @Test
    void laBarraDeLaHomeMuestraElRankingDeLigasAunqueNoHayaSesion() throws Exception {
        String html = mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("href=\"/ligas/clasificacion\"")
                .contains("class=\"ranking-ligas")
                .contains("Ranking</span><small>Ligas</small>");
        int rankingGeneral = html.indexOf("href=\"/ranking\"");
        int rankingLigas = html.indexOf("href=\"/ligas/clasificacion\"");
        assertThat(html.indexOf("</a>", rankingGeneral)).isLessThan(rankingLigas);
    }

    /** CA-98.1: cada cuota dice a qué resultado es, no solo el número. */
    @Test
    void cadaCuotaDiceAQueResultadoEs() throws Exception {
        partido("Getafe CF", "Sevilla FC");
        mvc.perform(get("/eventos"))
                .andExpect(content().string(containsString("class=\"solo-lectores\">gana Getafe CF, cuota ")))
                .andExpect(content().string(containsString("class=\"solo-lectores\">empate, cuota ")))
                .andExpect(content().string(containsString("class=\"solo-lectores\">gana Sevilla FC, cuota ")))
                // El 1, la X y el 2 y el número suelto se ocultan al lector para que no los lea dos veces
                .andExpect(content().string(containsString("<small aria-hidden=\"true\">1</small>")));
    }

    /** CA-98.2: los iconos de la cabecera tienen nombre. */
    @Test
    void losIconosDeLaCabeceraTienenNombre() throws Exception {
        mvc.perform(get("/eventos").with(user("usuario@apuestas.es")))
                .andExpect(content().string(containsString("<span class=\"solo-lectores\">Avisos</span>")))
                .andExpect(content().string(containsString("aria-label=\"Modo oscuro\"")));
    }

    /** CA-98.3: los mensajes se anuncian: los errores al momento y el resto cuando el lector acaba. */
    @Test
    void losMensajesSeAnuncianAlLectorDePantalla() throws Exception {
        mvc.perform(get("/login").param("error", ""))
                .andExpect(content().string(containsString("<p role=\"alert\" class=\"alerta alerta-error\"")));
        mvc.perform(get("/login").param("registrado", ""))
                .andExpect(content().string(containsString("<p role=\"status\" class=\"alerta alerta-exito\"")));
    }

    /** CA-98.4: el script que asocia cada error a su campo se carga en todas las páginas, sin sesión. */
    @Test
    void elScriptQueAsociaLosErroresASuCampoEstaEnTodasLasPaginas() throws Exception {
        mvc.perform(get("/registro")).andExpect(content().string(containsString("/js/accesible")));
        mvc.perform(get("/js/accesible.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-describedby")))
                .andExpect(content().string(containsString("aria-invalid")));
    }

    /** CA-100.1: ganada o perdida se dice con texto, no solo con el color. */
    @Test
    void elHistorialDiceConTextoSiSeGanoOSePerdio() throws Exception {
        usuarioService.crear("lucia@ucm.es", "Lucía", "secreta123", Rol.USUARIO);
        Evento ganada = partido("Getafe CF", "Sevilla FC");
        Evento perdida = partido("Elche CF", "Levante UD");
        apuestas.apostar("lucia@ucm.es", ganada.getId(), Resultado.LOCAL, new BigDecimal("10"));
        apuestas.apostar("lucia@ucm.es", perdida.getId(), Resultado.LOCAL, new BigDecimal("10"));
        resolucion.introducirResultado(ganada.getId(), Resultado.LOCAL);
        resolucion.introducirResultado(perdida.getId(), Resultado.VISITANTE);
        mvc.perform(get("/apuestas").with(user("lucia@ucm.es")))
                .andExpect(content().string(containsString("class=\"estado ganada\">GANADA</span>")))
                .andExpect(content().string(containsString("class=\"estado perdida\">PERDIDA</span>")));
    }

    /** CA-100.2: las subidas llevan «+» y las bajadas «−», además del color. */
    @Test
    void lasSubidasYBajadasLlevanSigno() {
        assertThat(formato.variacion(new BigDecimal("2.34"))).isEqualTo("+2,3 %");
        assertThat(formato.variacion(new BigDecimal("-1.25"))).isEqualTo("-1,3 %");
    }
}

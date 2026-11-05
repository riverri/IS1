package es.ucm.fdi.is1.apuestas.anuncios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;
import jakarta.persistence.EntityManager;

/** HU-60, HU-61 y HU-62: anuncios internos con los anuncios de partida de la migración V12. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnunciosWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");
    private static final RequestPostProcessor COMO_CREADOR = user("creador@apuestas.es").roles("CREADOR");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AnuncioRepository anuncios;

    @Autowired
    private ClicAnuncioRepository clics;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private EntityManager entityManager;

    private Anuncio mercados;
    private Anuncio ligas;

    @BeforeEach
    void preparar() {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        mercados = deDestino("/mercados");
        ligas = deDestino("/ligas");
    }

    private Anuncio deDestino(String destino) {
        return anuncios.findAll().stream().filter(a -> a.getDestino().equals(destino)).findFirst().orElseThrow();
    }

    private static String enlace(Anuncio anuncio) {
        return "/anuncios/" + anuncio.getId() + "/ir";
    }

    @Test
    void unVisitanteVeElAnuncioPublicoDeMasPrioridadYCuentaLaImpresion() throws Exception {
        long antes = mercados.getImpresiones();

        // CA-60.1 y CA-60.3: en la portada hay dos de prioridad 5, pero el de ligas necesita sesión
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(enlace(mercados))))
                .andExpect(content().string(not(containsString(enlace(ligas)))));

        // La impresión se suma en la base de datos: refresh vuelve a leer el anuncio en lugar de usar la copia en memoria
        entityManager.refresh(mercados);
        assertThat(mercados.getImpresiones()).isGreaterThan(antes);
    }

    @Test
    void enUnaSeccionNuncaSaleSuPropioAnuncio() throws Exception {
        // CA-60.2: el de ligas está puesto en RANKING, CUENTA… y no en LIGAS, pero aunque lo estuviera no saldría
        for (int i = 0; i < 10; i++) {
            mvc.perform(get("/ligas").with(COMO_ANA))
                    .andExpect(content().string(not(containsString(enlace(ligas)))));
        }
    }

    @Test
    void alPulsarlo_apuntaElClicYLlevaASuSeccion() throws Exception {
        // CA-60.3 y CA-62.1
        mvc.perform(get(enlace(mercados)).param("desde", "/eventos").with(COMO_ANA))
                .andExpect(redirectedUrl("/mercados"));

        List<ClicAnuncio> deMercados = clics.findAll().stream()
                .filter(c -> c.getAnuncio().getId().equals(mercados.getId())).toList();
        assertThat(deMercados).hasSize(1);
        assertThat(deMercados.get(0).getOrigen()).isEqualTo("/eventos");
        assertThat(deMercados.get(0).getUsuario().getEmail()).isEqualTo(ANA);
    }

    @Test
    void unVisitanteTambienPuedePulsarlo() throws Exception {
        mvc.perform(get(enlace(mercados)).param("desde", "https://otra-web.com"))
                .andExpect(redirectedUrl("/mercados"));

        // El origen que no es de esta web no se guarda tal cual
        assertThat(clics.findAll()).extracting(ClicAnuncio::getOrigen).containsOnly("/");
    }

    @Test
    void unAnuncioDesactivadoNoSaleNiCuenta() throws Exception {
        mercados.activar(false);

        mvc.perform(get(enlace(mercados))).andExpect(redirectedUrl("/"));
        mvc.perform(get("/")).andExpect(content().string(not(containsString(enlace(mercados)))));
        assertThat(clics.countByAnuncio(mercados)).isZero();
    }

    @Test
    void elCreadorNoVeAnuncios() throws Exception {
        mvc.perform(get("/eventos").with(COMO_CREADOR))
                .andExpect(content().string(not(containsString("class=\"anuncio\""))));
    }

    @Test
    void elCreadorCreaUnAnuncioQueSaleEnSusSecciones() throws Exception {
        // CA-61.1
        mvc.perform(post("/gestion/anuncios/nuevo").with(COMO_CREADOR).with(csrf())
                        .param("titulo", "Mira tus movimientos").param("texto", "Cada monedita, explicada")
                        .param("icono", "💰").param("destino", "/cuenta/movimientos")
                        .param("ubicaciones", "MIS_APUESTAS").param("prioridad", "10"))
                .andExpect(redirectedUrl("/gestion/anuncios"));

        Anuncio nuevo = deDestino("/cuenta/movimientos");
        mvc.perform(get("/apuestas").with(COMO_ANA))
                .andExpect(content().string(containsString(enlace(nuevo))))
                .andExpect(content().string(containsString("Cada monedita, explicada")));
    }

    @Test
    void noSeAdmitenDestinosDeFueraNiSinSecciones() throws Exception {
        long antes = anuncios.count();

        // CA-61.2
        mvc.perform(post("/gestion/anuncios/nuevo").with(COMO_CREADOR).with(csrf())
                        .param("titulo", "Fuera").param("texto", "Otra web").param("destino", "https://otra-web.com")
                        .param("ubicaciones", "PORTADA").param("prioridad", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tiene que ser una sección de la web")));
        mvc.perform(post("/gestion/anuncios/nuevo").with(COMO_CREADOR).with(csrf())
                        .param("titulo", "Sin sitio").param("texto", "Texto").param("destino", "/ranking")
                        .param("prioridad", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Elige al menos una sección")));

        assertThat(anuncios.count()).isEqualTo(antes);
    }

    @Test
    void enGestionSeVenLasImpresionesYLosClicsYSePuedeDesactivar() throws Exception {
        mvc.perform(get(enlace(mercados)).param("desde", "/eventos"));

        // CA-62.1
        mvc.perform(get("/gestion/anuncios").with(COMO_CREADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Apuestas a largo plazo")))
                .andExpect(content().string(containsString("% clics")));

        mvc.perform(post("/gestion/anuncios/" + mercados.getId() + "/activar").with(COMO_CREADOR).with(csrf())
                        .param("activo", "false"))
                .andExpect(redirectedUrl("/gestion/anuncios"));
        assertThat(anuncios.findById(mercados.getId()).orElseThrow().isActivo()).isFalse();
    }

    @Test
    void unJugadorNoEntraEnLaGestionDeAnuncios() throws Exception {
        mvc.perform(get("/gestion/anuncios").with(COMO_ANA)).andExpect(status().isForbidden());
    }
}

package es.ucm.fdi.is1.apuestas.anuncios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;
import jakarta.persistence.EntityManager;

/** Cuarta depuración: impresiones solo de páginas que se ven, destinos según la seguridad de verdad y clics vigentes. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImpresionesYDestinosWebTest {

    private static final String ANA = "ana-impresiones@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");

    @Autowired private MockMvc mvc;
    @Autowired private AnuncioRepository anuncios;
    @Autowired private ClicAnuncioRepository clics;
    @Autowired private UsuarioService usuarios;
    @Autowired private AnuncioService servicioAnuncios;
    @Autowired private EntityManager em;

    @BeforeEach
    void preparar() {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        // Solo los de esta prueba
        anuncios.findAll().forEach(a -> a.activar(false));
    }

    private Anuncio anuncio(String destino, Ubicacion donde) {
        return anuncios.save(new Anuncio("Prueba " + destino, "Texto", null, destino, Set.of(donde), 10, null, null));
    }

    private long impresiones(Anuncio anuncio) {
        em.flush();
        em.clear();
        return anuncios.findById(anuncio.getId()).orElseThrow().getImpresiones();
    }

    @Test
    void soloCuentaLaImpresionSiSePintaLaPagina() throws Exception {
        Anuncio enCuenta = anuncio("/ranking", Ubicacion.CUENTA);
        long antes = impresiones(enCuenta);

        mvc.perform(get("/cuenta/movimientos/exportar").with(COMO_ANA)).andExpect(status().isOk());
        assertThat(impresiones(enCuenta)).as("una descarga CSV no la ve nadie").isEqualTo(antes);

        mvc.perform(get("/cuenta/movimientos").with(COMO_ANA))
                .andExpect(content().string(containsString("/anuncios/" + enCuenta.getId() + "/ir")));
        servicioAnuncios.vaciarImpresiones();
        assertThat(impresiones(enCuenta)).isEqualTo(antes + 1);
    }

    @Test
    void aUnVisitanteNoSeLeMandaAUnaPaginaQuePideSesion() throws Exception {
        Anuncio cartera = anuncio("/cripto/cartera", Ubicacion.EQUIPOS);
        mvc.perform(get("/equipos")).andExpect(content().string(not(containsString("/anuncios/" + cartera.getId() + "/ir"))));
        mvc.perform(get("/equipos").with(COMO_ANA))
                .andExpect(content().string(containsString("/anuncios/" + cartera.getId() + "/ir")));
    }

    @Test
    void aUnVisitanteSiSeLeEnsenaUnaPaginaPublicaDeUnaSeccionConSesion() throws Exception {
        // /ligas pide sesión, pero /ligas/clasificacion es pública (SeguridadConfig)
        Anuncio clasificacion = anuncio("/ligas/clasificacion", Ubicacion.EQUIPOS);
        mvc.perform(get("/equipos"))
                .andExpect(content().string(containsString("/anuncios/" + clasificacion.getId() + "/ir")))
                .andExpect(content().string(containsString("rel=\"nofollow\"")));
    }

    @Test
    void unDestinoConFiltrosTambienSeEnsenaAUnVisitante() throws Exception {
        Anuncio futbol = anuncio("/eventos?deporte=FUTBOL", Ubicacion.EQUIPOS);
        mvc.perform(get("/equipos"))
                .andExpect(content().string(containsString("/anuncios/" + futbol.getId() + "/ir")));
    }

    @Test
    void unDestinoConPuntosNoEsDeNingunaSeccion() {
        assertThat(Ubicacion.de("/cuenta/../tema")).isEmpty();
        assertThat(Ubicacion.de("/eventos/./1")).isEmpty();
        assertThat(Ubicacion.de("/eventos\\otra")).isEmpty();
        assertThat(Ubicacion.de("/eventos/7/apostar")).contains(Ubicacion.EVENTOS);
    }

    @Test
    void unAnuncioCaducadoNoSumaClics() throws Exception {
        Anuncio caducado = anuncios.save(new Anuncio("Caducado", "Texto", null, "/ranking", Set.of(Ubicacion.PORTADA), 10,
                Hora.ahora().minusDays(10), Hora.ahora().minusDays(1)));
        long antes = clics.count();
        mvc.perform(get("/anuncios/" + caducado.getId() + "/ir"));
        assertThat(clics.count()).isEqualTo(antes);
    }
}

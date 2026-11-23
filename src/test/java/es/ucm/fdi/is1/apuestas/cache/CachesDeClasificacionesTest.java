package es.ucm.fdi.is1.apuestas.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.anuncios.AnuncioRepository;
import es.ucm.fdi.is1.apuestas.anuncios.AnuncioService;
import es.ucm.fdi.is1.apuestas.anuncios.Ubicacion;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.apuesta.CriterioRanking;
import es.ucm.fdi.is1.apuestas.apuesta.PuestoRanking;
import es.ucm.fdi.is1.apuestas.apuesta.RankingService;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.JugadorRepository;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.fantasy.FantasyService;
import es.ucm.fdi.is1.apuestas.mercados.MercadoService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;
import jakarta.persistence.EntityManagerFactory;

/**
 * Las cachés del ranking y de la clasificación fantasy, sin transacción de prueba: cada servicio confirma sus cambios
 * como en la aplicación de verdad, que es cuando la caché tiene que dejar de valer. Base de datos propia, que se tira
 * al acabar, para no dejar jugadores en las demás pruebas.
 */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:caches-clasificaciones",
        "spring.jpa.properties.hibernate.generate_statistics=true"})
@AutoConfigureMockMvc
@DirtiesContext
class CachesDeClasificacionesTest {

    @Autowired private MockMvc mvc;
    @Autowired private EntityManagerFactory emf;
    @Autowired private UsuarioService usuarios;
    @Autowired private RankingService rankings;
    @Autowired private ApuestaService apuestas;
    @Autowired private ResolucionService resolucion;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private EventoRepository eventos;
    @Autowired private JugadorRepository jugadores;
    @Autowired private FantasyService fantasy;
    @Autowired private MercadoService mercados;
    @Autowired private AnuncioService anuncios;
    @Autowired private AnuncioRepository anuncioRepository;

    private long consultasDe(Runnable accion) {
        Statistics estadisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estadisticas.clear();
        accion.run();
        return estadisticas.getPrepareStatementCount();
    }

    private Evento partido(String sufijo) {
        var liga = competiciones.findByNombre("LaLiga").orElseThrow();
        Equipo local = equipos.save(new Equipo("Local cache " + sufijo, Deporte.FUTBOL, 6.0));
        Equipo visitante = equipos.save(new Equipo("Visitante cache " + sufijo, Deporte.FUTBOL, 5.0));
        return eventos.save(new Evento(liga, local, visitante, Hora.ahora().plusDays(10)));
    }

    private static int posicion(List<PuestoRanking> puestos, String nombre) {
        return puestos.stream().filter(p -> p.nombre().equals(nombre)).findFirst().orElseThrow().posicion();
    }

    @Test
    void elRankingSeGuardaHastaQueCambiaUnSaldoYCoincideConElPerfil() {
        usuarios.crear("alba-cache@ucm.es", "Alba cache", "secreta123", Rol.USUARIO);
        usuarios.crear("bruno-cache@ucm.es", "Bruno cache", "secreta123", Rol.USUARIO);
        Evento evento = partido("ranking");

        var primera = rankings.lista(null, CriterioRanking.SALDO, null);
        long repetida = consultasDe(() -> rankings.lista(null, CriterioRanking.SALDO, null));
        assertThat(repetida).as("la segunda vez no se recalcula el ranking").isZero();
        // Mismo saldo: Alba va delante por nombre
        assertThat(posicion(primera.primeros(), "Alba cache")).isLessThan(posicion(primera.primeros(), "Bruno cache"));

        // Alba apuesta (baja su saldo): el ranking tiene que verlo enseguida, y el perfil, dar el mismo puesto
        apuestas.apostar("alba-cache@ucm.es", evento.getId(), Resultado.LOCAL, new BigDecimal("100"));
        var despues = rankings.lista(null, CriterioRanking.SALDO, null);
        assertThat(posicion(despues.primeros(), "Alba cache")).isGreaterThan(posicion(despues.primeros(), "Bruno cache"));
        Long alba = despues.primeros().stream().filter(p -> p.nombre().equals("Alba cache")).findFirst().orElseThrow()
                .usuarioId();
        assertThat(rankings.perfil(alba, null).puesto().posicion())
                .isEqualTo(posicion(despues.primeros(), "Alba cache"));
    }

    @Test
    void laClasificacionFantasyCambiaAlFinalizarUnPartidoDeUnClubFichado() {
        usuarios.crear("carla-cache@ucm.es", "Carla cache", "secreta123", Rol.USUARIO);
        Evento evento = partido("fantasy");
        Jugador delantero = new Jugador(evento.getLocal(), "Delantero cache", Posicion.DELANTERO);
        delantero.cambiarNota(6);
        jugadores.save(delantero);
        fantasy.crear("carla-cache@ucm.es", "Carla FC");
        fantasy.fichar("carla-cache@ucm.es", delantero.getId());

        assertThat(fantasy.clasificacion("carla-cache@ucm.es")).singleElement()
                .satisfies(p -> assertThat(p.puntos()).isZero());
        long repetida = consultasDe(() -> fantasy.clasificacion("carla-cache@ucm.es"));
        assertThat(repetida).as("solo la lista de equipos, sin volver a sumar los puntos").isEqualTo(1);

        // Gana 2-0 en casa: 3 por la victoria y 2 por cada gol de su delantero
        resolucion.introducirMarcador(evento.getId(), 2, 0);
        assertThat(fantasy.clasificacion("carla-cache@ucm.es")).singleElement()
                .satisfies(p -> assertThat(p.puntos()).isEqualTo(7));
        // Corregido a 0-1: ya no suma nada
        resolucion.introducirMarcador(evento.getId(), 0, 1);
        assertThat(fantasy.clasificacion("carla-cache@ucm.es")).singleElement()
                .satisfies(p -> assertThat(p.puntos()).isZero());
    }

    @Test
    void losMercadosAbiertosPintanElEscudoDeSusCandidatosSinTransaccionAbierta() throws Exception {
        Equipo equipo = equipos.save(new Equipo("Candidato con escudo", Deporte.FUTBOL, 6.0));
        mercados.crear("Mercado con escudo", Deporte.FUTBOL, Hora.ahora().plusDays(5),
                equipo.getNombre() + ";2.5\nOtro candidato;3");
        mvc.perform(get("/mercados")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Candidato con escudo")));
    }

    @Test
    void lasImpresionesPendientesSeGuardanAlApagar() {
        var anuncio = anuncios.crear("Anuncio cache", "Texto", "⭐", "/eventos", Set.of(Ubicacion.PORTADA), 1,
                null, null);
        anuncios.registrarImpresion(anuncio.getId());
        anuncios.registrarImpresion(anuncio.getId());
        anuncios.guardarAlApagar();
        assertThat(anuncioRepository.findById(anuncio.getId()).orElseThrow().getImpresiones()).isEqualTo(2);
    }
}

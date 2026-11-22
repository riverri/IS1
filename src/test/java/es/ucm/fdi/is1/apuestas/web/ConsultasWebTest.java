package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.anuncios.AnuncioService;
import es.ucm.fdi.is1.apuestas.apuesta.SeleccionPedida;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquiposService;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.eventos.CatalogoService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Número de consultas a la base de datos de las páginas con listas: no puede crecer con el número de filas.
 * Si crece, alguna fila hace su propia consulta (el problema «N+1»): con 10 filas son 10 consultas de más, con 500
 * son 500, y la página se vuelve lenta sin que ninguna prueba falle. Se mide la misma página con pocas filas y con
 * 10 más, y el número tiene que ser el mismo, como mucho con una consulta más: las colecciones se cargan de 50 en 50
 * (hibernate.default_batch_fetch_size) y al pasar de 50 filas hace falta otro lote. Una consulta por fila serían 10.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Transactional
@DirtiesContext
class ConsultasWebTest {

    private static final String ANA = "ana-consultas@ucm.es";

    @Autowired private MockMvc mvc;
    @Autowired private EntityManager em;
    @Autowired private EntityManagerFactory emf;
    @Autowired private UsuarioService usuarios;
    @Autowired private ApuestaService apuestas;
    @Autowired private EventoRepository eventos;
    @Autowired private CatalogoService catalogo;
    @Autowired private AnuncioService anuncios;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private EquiposService listadoEquipos;

    private final List<Evento> creados = new ArrayList<>();

    @BeforeEach
    void preparar() {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
    }

    /** Partidos nuevos entre equipos nuevos, para que ninguno venga ya en la caché de otro. */
    private void crearPartidos(int cuantos) {
        var liga = competiciones.findByNombre("LaLiga").orElseThrow();
        for (int i = 0; i < cuantos; i++) {
            int n = creados.size();
            Equipo local = equipos.save(new Equipo("Local consultas " + n, liga.getDeporte(), 6.0));
            Equipo visitante = equipos.save(new Equipo("Visitante consultas " + n, liga.getDeporte(), 5.0));
            creados.add(eventos.save(new Evento(liga, local, visitante, Hora.ahora().plusDays(1).plusMinutes(n))));
        }
    }

    /** Una simple y una combinada de Ana en cada pareja de partidos nuevos. */
    private void apostar(int desde) {
        for (int i = desde; i + 1 < creados.size(); i += 2) {
            apuestas.apostar(ANA, creados.get(i).getId(), Resultado.LOCAL, BigDecimal.ONE);
            apuestas.apostar(ANA, List.of(new SeleccionPedida(creados.get(i).getId(), Resultado.VISITANTE, null),
                    new SeleccionPedida(creados.get(i + 1).getId(), Resultado.LOCAL, null)), BigDecimal.ONE);
        }
    }

    /** Lo mismo, o una más por pasar de un lote de 50 (ver arriba). */
    private static void sinConsultaPorFila(long conPocas, long conMuchas, String pagina) {
        assertThat(conMuchas).as("consultas de %s con 10 filas más (con pocas: %d)", pagina, conPocas)
                .isLessThanOrEqualTo(conPocas + 1);
    }

    /** Consultas que hace una petición, empezando sin nada en la caché de la sesión. */
    private long consultas(MockHttpServletRequestBuilder peticion) throws Exception {
        em.flush();
        em.clear();
        anuncios.invalidarCacheAnuncios();
        Statistics estadisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estadisticas.clear();
        mvc.perform(peticion).andExpect(status().isOk());
        return estadisticas.getPrepareStatementCount();
    }

    @Test
    void elCatalogoHaceLasMismasConsultasConMasPartidos() throws Exception {
        crearPartidos(2);
        long conPocos = consultas(get("/eventos"));
        crearPartidos(10);
        long conMuchos = consultas(get("/eventos"));
        sinConsultaPorFila(conPocos, conMuchos, "catálogo");
    }

    @Test
    void elCatalogoPaginaYFiltraEnBaseDeDatosYConservaBusquedaSinTildes() {
        crearPartidos(55);
        var primera = catalogo.buscar(null, "Local consultas", 0);
        var segunda = catalogo.buscar(null, "Local consultas", 1);

        assertThat(primera.eventos().values().stream().flatMap(competiciones -> competiciones.values().stream())
                .mapToInt(List::size).sum()).isEqualTo(50);
        assertThat(primera.siguiente()).isTrue();
        assertThat(segunda.eventos().values().stream().flatMap(competiciones -> competiciones.values().stream())
                .mapToInt(List::size).sum()).isEqualTo(5);
        assertThat(segunda.siguiente()).isFalse();

        var liga = competiciones.findByNombre("LaLiga").orElseThrow();
        Equipo atletico = equipos.save(new Equipo("Atlético consultas", liga.getDeporte(), 6.0));
        Equipo visitante = equipos.save(new Equipo("Rival consultas", liga.getDeporte(), 5.0));
        eventos.save(new Evento(liga, atletico, visitante, Hora.ahora().plusDays(2)));
        var busqueda = catalogo.buscar(null, "atletico consultas", 0);
        assertThat(busqueda.eventos().values().stream().flatMap(competiciones -> competiciones.values().stream())
                .flatMap(List::stream).map(Evento::getLocal).map(Equipo::getNombre))
                .contains("Atlético consultas");
    }

    @Test
    void elListadoDeEquiposPaginaYFiltraEnBaseDeDatos() {
        for (int i = 0; i < 55; i++) {
            equipos.save(new Equipo("Clubes consultas " + i, es.ucm.fdi.is1.apuestas.equipos.Deporte.FUTBOL, 5.0));
        }
        var primera = listadoEquipos.buscar(null, null, "Clubes consultas", 0);
        var segunda = listadoEquipos.buscar(null, null, "Clubes consultas", 1);

        assertThat(primera.equipos().values().stream().mapToInt(List::size).sum()).isEqualTo(50);
        assertThat(primera.siguiente()).isTrue();
        assertThat(segunda.equipos().values().stream().mapToInt(List::size).sum()).isEqualTo(5);
        assertThat(segunda.siguiente()).isFalse();
    }

    @Test
    void misApuestasHaceLasMismasConsultasConMasApuestas() throws Exception {
        crearPartidos(2);
        apostar(0);
        long conPocas = consultas(get("/apuestas").with(user(ANA)));
        crearPartidos(10);
        apostar(2);
        long conMuchas = consultas(get("/apuestas").with(user(ANA)));
        sinConsultaPorFila(conPocas, conMuchas, "Mis apuestas");
    }

    @Test
    void misApuestasLimitaActivasYHistorialAPaginasDeVeinte() {
        crearPartidos(2);
        for (int i = 0; i < 21; i++) {
            apuestas.apostar(ANA, creados.getFirst().getId(), Resultado.LOCAL, BigDecimal.ONE);
        }

        var primera = apuestas.paginaActivas(ANA, 0);
        var segunda = apuestas.paginaActivas(ANA, 1);

        assertThat(primera.apuestas()).hasSize(20);
        assertThat(primera.siguiente()).isTrue();
        assertThat(segunda.apuestas()).hasSize(1);
        assertThat(segunda.siguiente()).isFalse();
        assertThat(apuestas.importeActivo(ANA)).isEqualByComparingTo("21.00");
        assertThat(apuestas.cuentaActivas(ANA)).isEqualTo(21);
    }

    private void crearUsuarios(int desde, int cuantos) {
        for (int i = desde; i < desde + cuantos; i++) {
            usuarios.crear("jugador-consultas-" + i + "@ucm.es", "Jugador " + i, "secreta123", Rol.USUARIO);
        }
    }

    @Test
    void misApuestasHaceLasMismasConsultasAunqueLasOfertasSubanDeLoApostado() throws Exception {
        // Otro jugador apuesta mucho a lo mismo: bajan las cuotas y las ofertas de cobro anticipado superan lo
        // apostado, que es cuando se comprueba si Ana tiene otras apuestas en esos partidos
        usuarios.crear("luis-consultas@ucm.es", "Luis", "secreta123", Rol.USUARIO);
        crearPartidos(2);
        apostar(0);
        apuestas.apostar("luis-consultas@ucm.es", creados.get(0).getId(), Resultado.LOCAL, new BigDecimal("300"));
        long conPocas = consultas(get("/apuestas").with(user(ANA)));
        crearPartidos(10);
        apostar(2);
        for (int i = 2; i < creados.size(); i += 2) {
            apuestas.apostar("luis-consultas@ucm.es", creados.get(i).getId(), Resultado.LOCAL, new BigDecimal("50"));
        }
        long conMuchas = consultas(get("/apuestas").with(user(ANA)));
        sinConsultaPorFila(conPocas, conMuchas, "Mis apuestas con ofertas al alza");
    }

    @Test
    void elRankingHaceLasMismasConsultasConMasJugadores() throws Exception {
        crearUsuarios(0, 2);
        long conPocos = consultas(get("/ranking"));
        crearUsuarios(2, 10);
        long conMuchos = consultas(get("/ranking"));
        sinConsultaPorFila(conPocos, conMuchos, "ranking");
    }

    @Test
    void elPanelDelCreadorHaceLasMismasConsultasConMasPartidos() throws Exception {
        crearPartidos(2);
        apostar(0);
        var creador = user("creador@apuestas.es").roles("CREADOR");
        long conPocos = consultas(get("/gestion").with(creador));
        crearPartidos(10);
        apostar(2);
        long conMuchos = consultas(get("/gestion").with(creador));
        sinConsultaPorFila(conPocos, conMuchos, "panel");
    }

    @Test
    void misMovimientosHaceLasMismasConsultasConMasMovimientos() throws Exception {
        crearPartidos(2);
        apostar(0);
        long conPocos = consultas(get("/cuenta/movimientos").with(user(ANA)));
        crearPartidos(10);
        apostar(2);
        long conMuchos = consultas(get("/cuenta/movimientos").with(user(ANA)));
        sinConsultaPorFila(conPocos, conMuchos, "Mis movimientos");
    }
}

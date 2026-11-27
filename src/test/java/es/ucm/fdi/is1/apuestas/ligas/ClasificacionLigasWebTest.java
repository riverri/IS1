package es.ucm.fdi.is1.apuestas.ligas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Ranking de ligas: vista activa sin mezclar filas (CA-111.4), desempate estable en la global (CA-112.2) y cambio de
 * visibilidad sin tocar miembros, retos ni bloqueos (CA-113.5).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClasificacionLigasWebTest {

    private static final String ANA = "ana-clasif-ligas@ucm.es";
    private static final String LUIS = "luis-clasif-ligas@ucm.es";
    private static final String EVA = "eva-clasif-ligas@ucm.es";

    /** Pestaña marcada como activa: {@code <a class="pestana activa" ...>Texto</a>}. */
    private static final Pattern PESTANA_ACTIVA = Pattern.compile("<a class=\"pestana activa\"[^>]*>([^<]*)</a>");
    private static final Pattern PESTANA = Pattern.compile("<a class=\"pestana[^\"]*\"");
    private static final Pattern FILA = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL);
    private static final Pattern CELDA = Pattern.compile("<td[^>]*>(.*?)</td>", Pattern.DOTALL);

    @Autowired private MockMvc mvc;
    @Autowired private LigaService ligas;
    @Autowired private LigaRepository ligaRepository;
    @Autowired private RetoService retos;
    @Autowired private BloqueoLigaService bloqueos;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private EventoRepository eventos;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        usuarioService.crear(EVA, "Eva", "secreta123", Rol.USUARIO);
    }

    private String html(MockHttpServletRequestBuilder peticion) throws Exception {
        return mvc.perform(peticion).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    /** Texto de las pestañas marcadas como activas. */
    private static List<String> pestanasActivas(String html) {
        List<String> activas = new ArrayList<>();
        Matcher m = PESTANA_ACTIVA.matcher(html);
        while (m.find()) {
            activas.add(m.group(1).trim());
        }
        return activas;
    }

    private static long pestanas(String html) {
        return PESTANA.matcher(html).results().count();
    }

    /** Celdas, ya sin etiquetas, de cada fila del cuerpo de la tabla de clasificación. */
    private static List<List<String>> filas(String html) {
        int inicio = html.indexOf("<tbody>");
        if (inicio < 0) {
            return List.of();
        }
        String cuerpo = html.substring(inicio, html.indexOf("</tbody>", inicio));
        List<List<String>> filas = new ArrayList<>();
        Matcher fila = FILA.matcher(cuerpo);
        while (fila.find()) {
            List<String> celdas = new ArrayList<>();
            Matcher celda = CELDA.matcher(fila.group(1));
            while (celda.find()) {
                celdas.add(celda.group(1).replaceAll("<[^>]+>", "").trim());
            }
            filas.add(celdas);
        }
        return filas;
    }

    /** Nombre de la liga de cada fila (segunda columna), en el orden en que se muestran. */
    private static List<String> nombres(String html) {
        return filas(html).stream().map(f -> f.get(1)).toList();
    }

    private Liga publica(String email, String nombre) {
        Liga liga = ligas.crear(email, nombre);
        ligas.cambiarVisibilidad(liga.getId(), email, true);
        return liga;
    }

    // --- CA-111.4 ---

    @Test
    void alCambiarDeVistaQuedaMarcadaLaActivaYNoSeMezclanLasFilas() throws Exception {
        ligas.crear(ANA, "Privada de Ana");
        publica(ANA, "Pública de Ana");
        // Pública, pero Ana no es miembro: solo puede salir en la global
        publica(EVA, "Pública de Eva");

        String mias = html(get("/ligas/clasificacion").param("vista", "mias").with(user(ANA)));
        assertThat(pestanas(mias)).isEqualTo(2);
        assertThat(pestanasActivas(mias)).containsExactly("Mis ligas");
        assertThat(mias).contains("Suma del saldo de los miembros activos de tus ligas")
                .doesNotContain("Ligas públicas ordenadas por la suma");
        assertThat(nombres(mias)).containsExactlyInAnyOrder("Privada de Ana", "Pública de Ana");

        String global = html(get("/ligas/clasificacion").param("vista", "global").with(user(ANA)));
        assertThat(pestanasActivas(global)).containsExactly("Global");
        assertThat(global).contains("Ligas públicas ordenadas por la suma")
                .doesNotContain("Suma del saldo de los miembros activos de tus ligas", "Visibilidad");
        assertThat(nombres(global)).containsExactlyInAnyOrder("Pública de Ana", "Pública de Eva");
        // En la global no hay columna de visibilidad: cada fila tiene puesto, liga, miembros y saldo
        assertThat(filas(global)).allSatisfy(f -> assertThat(f).hasSize(4));
        assertThat(filas(mias)).allSatisfy(f -> assertThat(f).hasSize(5));

        // Volver a la otra vista, o recargar, marca la misma pestaña y da las mismas filas que la primera vez
        for (int recarga = 0; recarga < 2; recarga++) {
            String otraVezMias = html(get("/ligas/clasificacion").param("vista", "mias").with(user(ANA)));
            assertThat(pestanasActivas(otraVezMias)).containsExactly("Mis ligas");
            assertThat(filas(otraVezMias)).isEqualTo(filas(mias));
            String otraVezGlobal = html(get("/ligas/clasificacion").param("vista", "global").with(user(ANA)));
            assertThat(pestanasActivas(otraVezGlobal)).containsExactly("Global");
            assertThat(filas(otraVezGlobal)).isEqualTo(filas(global));
        }
    }

    @Test
    void sinIndicarVistaOConUnaDesconocidaSeMarcaMisLigasYSinSesionSoloLaGlobal() throws Exception {
        ligas.crear(ANA, "Privada de Ana");
        publica(EVA, "Pública de Eva");

        for (String pagina : List.of(html(get("/ligas/clasificacion").with(user(ANA))),
                html(get("/ligas/clasificacion").param("vista", "otra").with(user(ANA))))) {
            assertThat(pestanasActivas(pagina)).containsExactly("Mis ligas");
            assertThat(nombres(pagina)).containsExactly("Privada de Ana");
        }

        // Sin sesión no hay «Mis ligas»: aunque se pida, la vista activa es la global y solo con sus filas
        String anonimo = html(get("/ligas/clasificacion").param("vista", "mias"));
        assertThat(pestanas(anonimo)).isEqualTo(1);
        assertThat(pestanasActivas(anonimo)).containsExactly("Global");
        assertThat(nombres(anonimo)).containsExactly("Pública de Eva");
    }

    // --- CA-112.2 ---

    @Test
    void conElMismoSaldoTotalElDesempateEsEstableYLaPosicionCoincideConElOrden() throws Exception {
        // Todas con 1.000 monedas por miembro: «Zeta» tiene dos miembros y va primera; las otras tres empatan
        Liga zeta = publica(EVA, "Zeta");
        ligas.unirse(LUIS, zeta.getCodigo());
        // Desempate por nombre sin distinguir mayúsculas: «alfa» antes que «Beta» (distinguiéndolas, iría después)
        Liga alfa = publica(EVA, "alfa");
        // «Beta» y «BETA» son el mismo nombre: decide el id, el de la liga creada antes
        Liga beta = publica(ANA, "Beta");
        Liga betaMayusculas = publica(LUIS, "BETA");
        assertThat(beta.getId()).isLessThan(betaMayusculas.getId());

        List<PuestoClasificacionLiga> clasificacion = ligas.clasificacionGlobal();
        assertThat(clasificacion).extracting(PuestoClasificacionLiga::ligaId)
                .containsExactly(zeta.getId(), alfa.getId(), beta.getId(), betaMayusculas.getId());
        assertThat(clasificacion).extracting(p -> p.saldoTotal().intValueExact()).containsExactly(2000, 1000, 1000, 1000);
        assertThat(clasificacion).extracting(PuestoClasificacionLiga::posicion).containsExactly(1, 2, 3, 4);
        // Estable: calcularla otra vez da el mismo orden
        assertThat(ligas.clasificacionGlobal()).isEqualTo(clasificacion);

        // En la página, el puesto de cada fila es su posición en la tabla, en el mismo orden
        String pagina = html(get("/ligas/clasificacion").param("vista", "global"));
        assertThat(filas(pagina)).extracting(f -> f.get(0) + " " + f.get(1))
                .containsExactly("1 Zeta", "2 alfa", "3 Beta", "4 BETA");
        assertThat(filas(html(get("/ligas/clasificacion").param("vista", "global")))).isEqualTo(filas(pagina));
    }

    // --- CA-113.5 ---

    @Test
    void cambiarLaVisibilidadNoCambiaMiembrosRetosNiBloqueos() throws Exception {
        Liga liga = ligas.crear(ANA, "Con de todo");
        ligas.unirse(LUIS, liga.getCodigo());
        ligas.unirse(EVA, liga.getCodigo());
        Usuario luis = usuarios.findByEmail(LUIS).orElseThrow();
        Usuario eva = usuarios.findByEmail(EVA).orElseThrow();
        Evento partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(), equipos.findByNombre("Sevilla FC").orElseThrow(),
                Hora.ahora().plusDays(1)));
        // Un reto pendiente y otro aceptado, un bloqueo a un compañero y otro a toda la liga
        Reto pendiente = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        Reto aceptado = retos.retar(EVA, luis.getId(), partido.getId(), Resultado.VISITANTE, BigDecimal.TEN);
        retos.aceptar(LUIS, aceptado.getId(), Resultado.LOCAL);
        bloqueos.crear(liga.getId(), ANA, luis.getId(), partido.getId(), "LOCAL");
        bloqueos.crearParaLiga(liga.getId(), EVA, partido.getId(), "VISITANTE");

        String miembrosAntes = miembros(liga);
        List<String> retosAntes = retos(liga);
        List<String> bloqueosAntes = bloqueos(liga);
        Map<String, BigDecimal> saldosAntes = saldos();
        assertThat(retosAntes).hasSize(2);
        assertThat(bloqueosAntes).hasSize(2);

        for (boolean publica : List.of(true, false)) {
            mvc.perform(post("/ligas/{id}/visibilidad", liga.getId()).with(user(ANA)).with(csrf())
                            .param("publica", String.valueOf(publica)))
                    .andExpect(redirectedUrl("/ligas/" + liga.getId()));

            assertThat(ligaRepository.findById(liga.getId()).orElseThrow().isPublica()).isEqualTo(publica);
            // Solo cambia su inclusión en la vista global
            assertThat(ligas.clasificacionGlobal().stream().anyMatch(p -> p.ligaId().equals(liga.getId())))
                    .isEqualTo(publica);
            assertThat(miembros(liga)).isEqualTo(miembrosAntes);
            assertThat(retos(liga)).isEqualTo(retosAntes);
            assertThat(bloqueos(liga)).isEqualTo(bloqueosAntes);
            assertThat(saldos()).isEqualTo(saldosAntes);
            // Los bloqueos siguen funcionando: Luis no puede apostar al local ni Ana al visitante
            assertThatThrownBy(() -> bloqueos.comprobar(luis, partido, Resultado.LOCAL, null))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("bloqueada");
            Usuario ana = usuarios.findByEmail(ANA).orElseThrow();
            assertThatThrownBy(() -> bloqueos.comprobar(ana, partido, Resultado.VISITANTE, null))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("bloqueada");
            bloqueos.comprobar(eva, partido, Resultado.VISITANTE, null);
        }
        assertThat(retos(liga)).extracting(r -> r.substring(r.indexOf(':') + 1))
                .containsExactlyInAnyOrder(EstadoReto.PENDIENTE.name(), EstadoReto.ACEPTADO.name());
        assertThat(pendiente.getEstado()).isEqualTo(EstadoReto.PENDIENTE);
    }

    private String miembros(Liga liga) {
        return ligaRepository.findById(liga.getId()).orElseThrow().getIdsMiembros().stream().sorted().toList().toString();
    }

    /** Retos entre miembros de la liga, como «id:estado», ordenados. */
    private List<String> retos(Liga liga) {
        Set<Long> miembros = ligaRepository.findById(liga.getId()).orElseThrow().getIdsMiembros();
        return Stream.of(ANA, LUIS, EVA).flatMap(email -> retos.mios(email).stream())
                .filter(r -> miembros.contains(r.getRetador().getId()) && miembros.contains(r.getRetado().getId()))
                .map(r -> r.getId() + ":" + r.getEstado().name()).distinct().sorted().toList();
    }

    /** Bloqueos vigentes de la liga, como «id:destinatario:alcance», ordenados por id. */
    private List<String> bloqueos(Liga liga) {
        return bloqueos.deLiga(ligaRepository.findById(liga.getId()).orElseThrow()).stream()
                .map(b -> b.getId() + ":" + (b.isParaTodaLaLiga() ? "TODOS" : b.getObjetivo().getId()) + ":" + b.getAlcance())
                .sorted().toList();
    }

    private Map<String, BigDecimal> saldos() {
        return Map.of(ANA, saldoDe(ANA), LUIS, saldoDe(LUIS), EVA, saldoDe(EVA));
    }

    private BigDecimal saldoDe(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo().stripTrailingZeros();
    }
}

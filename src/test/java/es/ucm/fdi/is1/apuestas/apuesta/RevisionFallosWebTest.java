package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.JugadorRepository;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaRepository;
import es.ucm.fdi.is1.apuestas.ligas.LigaService;
import es.ucm.fdi.is1.apuestas.mercados.MercadoRepository;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** Fallos encontrados en la revisión del código (F-xx del informe y N-xx de la revisión), uno por prueba. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RevisionFallosWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA);
    private static final RequestPostProcessor CREADOR = user("creador@apuestas.es").roles("CREADOR");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ApuestaRepository apuestaRepository;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private RankingService rankings;

    @Autowired
    private VolumenApuestas volumen;

    @Autowired
    private CalculadoraCuotas calculadora;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private JugadorRepository jugadores;

    @Autowired
    private LigaService ligaService;

    @Autowired
    private LigaRepository ligas;

    @Autowired
    private MercadoRepository mercados;

    @Autowired
    private NotificacionService notificaciones;

    @Autowired
    private Clock reloj;

    private Evento futbol;
    private Evento otro;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        LocalDateTime manana = Hora.ahora().plusDays(1);
        futbol = partido("Getafe CF", "Sevilla FC", manana);
        otro = partido("Levante UD", "Elche CF", manana);
    }

    private Evento partido(String local, String visitante, LocalDateTime fecha) {
        return eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre(local).orElseThrow(), equipos.findByNombre(visitante).orElseThrow(), fecha));
    }

    private Usuario jugador(String email) {
        return usuarioService.crear(email, email.substring(0, email.indexOf('@')), "secreta123", Rol.USUARIO);
    }

    // --- F-01: el reloj va en hora de Madrid aunque el servidor esté en otra zona ---

    @Test
    void elRelojVaEnHoraDeMadrid() {
        assertThat(reloj.getZone()).isEqualTo(Hora.MADRID);
    }

    @Test
    void unPartidoEmpezadoEnHoraDeMadridYaNoAdmiteApuestas() {
        Evento empezado = partido("Real Betis", "CA Osasuna", Hora.ahora().minusMinutes(30));

        assertThatThrownBy(() -> apuestas.apostar(ANA, empezado.getId(), Resultado.LOCAL, new BigDecimal("10")))
                .isInstanceOf(es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException.class);
    }

    // --- F-03 y F-13: el volumen limita lo que aporta cada usuario y reparte las combinadas ---

    @Test
    void unSoloUsuarioNoPuedeMoverMuchoLasCuotas() {
        usuarios.findByEmail(ANA).orElseThrow().abonar(new BigDecimal("2000"));
        for (int i = 0; i < 4; i++) {
            apuestas.apostar(ANA, futbol.getId(), Resultado.LOCAL, new BigDecimal("500"));
        }

        assertThat(volumen.importes(futbol).get(Resultado.LOCAL)).isEqualByComparingTo("100");
    }

    @Test
    void unaCombinadaRepartaSuImporteEntreSusSelecciones() {
        apuestas.apostar(ANA, List.of(new SeleccionPedida(futbol.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(otro.getId(), Resultado.LOCAL, null)), new BigDecimal("60"));

        assertThat(volumen.importes(futbol).get(Resultado.LOCAL)).isEqualByComparingTo("30");
    }

    // --- F-06: tope de cuota total ---

    @Test
    void laCuotaTotalNoPuedePasarDeMil() {
        assertThatThrownBy(() -> ApuestaService.comprobarCuotaTotal(new BigDecimal("1000.01")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("1.000");
        ApuestaService.comprobarCuotaTotal(new BigDecimal("1000"));
    }

    // --- F-10: % de aciertos con un mínimo de apuestas ---

    @Test
    void enElPorcentajeDeAciertosVanPrimeroLosQueTienenSuficientesApuestas() {
        jugador("suerte@ucm.es");
        jugador("constante@ucm.es");
        Evento uno = partido("Real Betis", "CA Osasuna", Hora.ahora().plusDays(1));
        apuestas.apostar("suerte@ucm.es", uno.getId(), Resultado.LOCAL, new BigDecimal("10"));
        apuestas.apostar("constante@ucm.es", uno.getId(), Resultado.LOCAL, new BigDecimal("10"));
        resolucion.introducirResultado(uno.getId(), Resultado.LOCAL);
        String[][] pares = {{"Real Sociedad", "Valencia CF"}, {"Rayo Vallecano", "Athletic Club"},
            {"Deportivo Alavés", "Celta de Vigo"}, {"Málaga CF", "RCD Espanyol"}};
        for (int i = 0; i < pares.length; i++) {
            Evento e = partido(pares[i][0], pares[i][1], Hora.ahora().plusDays(1));
            apuestas.apostar("constante@ucm.es", e.getId(), Resultado.LOCAL, new BigDecimal("10"));
            resolucion.introducirResultado(e.getId(), i < 3 ? Resultado.LOCAL : Resultado.VISITANTE);
        }

        List<String> orden = rankings.ranking(null, CriterioRanking.ACIERTOS).stream()
                .map(PuestoRanking::nombre).filter(n -> n.equals("suerte") || n.equals("constante")).toList();
        assertThat(orden).containsExactly("constante", "suerte");
    }

    // --- F-13: una cuenta eliminada no recibe dinero ---

    @Test
    void unaCuentaEliminadaNoCobraLasApuestasQueQuedaban() {
        Usuario ana = usuarios.findByEmail(ANA).orElseThrow();
        ana.eliminar("x");
        ana.ajustar(new BigDecimal("50"));
        ana.abonar(new BigDecimal("50"));

        assertThat(ana.getSaldo()).isEqualByComparingTo("0");
    }

    // --- F-14: la consola de la base de datos solo para el creador ---

    @Test
    void laConsolaDeLaBaseDeDatosNoEsPublica() throws Exception {
        mvc.perform(get("/h2-console")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/h2-console").with(COMO_ANA)).andExpect(status().isForbidden());
    }

    // --- N-03: el creador no apuesta ---

    @Test
    void elCreadorNoPuedeApostar() throws Exception {
        assertThatThrownBy(() -> apuestas.apostar("creador@apuestas.es", futbol.getId(), Resultado.LOCAL,
                new BigDecimal("10"))).hasMessageContaining("no puede apostar");
        mvc.perform(post("/boleto/anadir").with(CREADOR).with(csrf())
                        .param("evento", futbol.getId().toString()).param("resultado", "LOCAL"))
                .andExpect(flash().attribute("errorBoleto", "El creador de apuestas no puede apostar"));
        mvc.perform(get("/eventos/{id}/apostar", futbol.getId()).with(CREADOR))
                .andExpect(content().string(containsString("El creador de apuestas no puede apostar")))
                .andExpect(content().string(not(containsString("Confirmar apuesta"))));
    }

    // --- N-05: la apuesta simple comprueba la cuota que vio el usuario ---

    @Test
    void siLaCuotaCambioSeAvisaEnLugarDeApostar() throws Exception {
        mvc.perform(post("/eventos/{id}/apostar", futbol.getId()).with(COMO_ANA).with(csrf())
                        .param("opcion", "LOCAL").param("importe", "10").param("cuotaVista", "99.99"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La cuota ha cambiado")));
        assertThat(apuestaRepository.findAll()).noneMatch(a -> a.getUsuario().getEmail().equals(ANA));

        String cuota = calculadora.calcular(futbol).local().toPlainString();
        mvc.perform(post("/eventos/{id}/apostar", futbol.getId()).with(COMO_ANA).with(csrf())
                        .param("opcion", "LOCAL").param("importe", "10").param("cuotaVista", cuota))
                .andExpect(redirectedUrl("/apuestas"));
    }

    // --- N-07: los nombres de jugadores no se meten en el JavaScript ---

    @Test
    void unNombreConApostrofoNoRompeLaConfirmacion() throws Exception {
        var getafe = equipos.findByNombre("Getafe CF").orElseThrow();
        jugadores.save(new Jugador(getafe, "N'Golo Kanté", Posicion.CENTROCAMPISTA));

        mvc.perform(get("/gestion/equipos/{id}/editar", getafe.getId()).with(CREADOR))
                .andExpect(content().string(containsString("data-nombre=\"N&#39;Golo Kant")))
                .andExpect(content().string(containsString("this.dataset.nombre")))
                .andExpect(content().string(not(containsString("confirm('¿Quitar a N"))));
    }

    // --- N-11: marcador y ganador contradictorios ---

    @Test
    void unMarcadorQueContradiceAlGanadorSeRechaza() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/resultado", futbol.getId()).with(CREADOR).with(csrf())
                        .param("golesLocal", "2").param("golesVisitante", "1").param("resultado", "VISITANTE"))
                .andExpect(flash().attribute("error", containsString("no coincide")));
        assertThat(futbol.getResultado()).isNull();
    }

    // --- N-12: "Aceptar nuevas cuotas" apuesta a la primera ---

    @Test
    void conLasCuotasQueSeVenElBoletoApuestaALaPrimera() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(post("/boleto/anadir").session(sesion).with(COMO_ANA).with(csrf())
                .param("evento", futbol.getId().toString()).param("resultado", "LOCAL"));
        // Otros jugadores mueven la cuota después de añadirla
        jugador("b@ucm.es");
        apuestas.apostar("b@ucm.es", futbol.getId(), Resultado.LOCAL, new BigDecimal("100"));
        String nueva = calculadora.calcular(futbol).local().toPlainString();

        mvc.perform(post("/boleto/confirmar").session(sesion).with(COMO_ANA).with(csrf())
                        .param("importe", "10").param("cuotaVista", futbol.getId() + ":" + nueva))
                .andExpect(redirectedUrl("/apuestas"));
    }

    // --- N-13: un partido que deja de admitir apuestas con el boleto abierto ---

    @Test
    void unPartidoSuspendidoConElBoletoAbiertoDaUnAvisoYNoUn404() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(post("/boleto/anadir").session(sesion).with(COMO_ANA).with(csrf())
                .param("evento", futbol.getId().toString()).param("resultado", "LOCAL"));
        resolucion.suspender(futbol.getId());

        mvc.perform(post("/boleto/confirmar").session(sesion).with(COMO_ANA).with(csrf()).param("importe", "10"))
                .andExpect(redirectedUrl("/boleto"))
                .andExpect(flash().attribute("error", containsString("ya no admite apuestas")));
    }

    @Test
    void unaCombinadaConUnPartidoSuspendidoNoSeConvierteEnSimple() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        for (Evento e : List.of(futbol, otro)) {
            mvc.perform(post("/boleto/anadir").session(sesion).with(COMO_ANA).with(csrf())
                    .param("evento", e.getId().toString()).param("resultado", "LOCAL"));
        }
        resolucion.suspender(otro.getId());

        mvc.perform(post("/boleto/confirmar").session(sesion).with(COMO_ANA).with(csrf()).param("importe", "10"))
                .andExpect(redirectedUrl("/boleto"));
        assertThat(apuestaRepository.findAll()).noneMatch(a -> a.getUsuario().getEmail().equals(ANA));
    }

    // --- N-14: las ligas de una cuenta eliminada ---

    @Test
    void alEliminarLaCuentaSeBorranSusLigasYSaleDeLasDemas() {
        jugador("luis@ucm.es");
        Liga suya = ligaService.crear(ANA, "La de Ana");
        Liga ajena = ligaService.crear("luis@ucm.es", "La de Luis");
        ligaService.unirse(ANA, ajena.getCodigo());

        ligaService.alEliminarCuenta(new es.ucm.fdi.is1.apuestas.usuarios.CuentaEliminada(
                usuarios.findByEmail(ANA).orElseThrow().getId()));

        assertThat(ligas.findById(suya.getId())).isEmpty();
        assertThat(ajena.getIdsMiembros()).doesNotContain(usuarios.findByEmail(ANA).orElseThrow().getId());
    }

    // --- N-08, N-09 y N-15: registro ---

    @Test
    void elDominioDeLasCuentasEliminadasEstaReservado() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("nombre", "Pillo").param("email", "eliminado-5@apuestas.invalid")
                        .param("password", "secreta123").param("confirmacion", "secreta123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe una cuenta con ese email")));
    }

    @Test
    void unaContrasenaConMuchasEnesNoDaError500() throws Exception {
        String larga = "ñ".repeat(40);
        mvc.perform(post("/registro").with(csrf()).param("nombre", "Ñoño").param("email", "nono@ucm.es")
                        .param("password", larga).param("confirmacion", larga))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("demasiado larga")));
    }

    @Test
    void unEmailDemasiadoLargoSeRechaza() throws Exception {
        // Email válido (etiquetas de 60 caracteres) pero más largo que la columna de la base de datos
        String email = "a".repeat(60) + "@" + ("b".repeat(60) + ".").repeat(4) + "es";
        mvc.perform(post("/registro").with(csrf()).param("nombre", "Largo").param("email", email)
                        .param("password", "secreta123").param("confirmacion", "secreta123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Máximo 255 caracteres")));
        assertThat(usuarios.findByEmail(email)).isEmpty();
    }

    @Test
    void unCandidatoConNombreLarguisimoSeRechaza() throws Exception {
        Long mercado = mercados.findAll().get(0).getId();
        mvc.perform(post("/gestion/mercados/{id}/candidatos", mercado).with(CREADOR).with(csrf())
                        .param("nombre", "x".repeat(300)).param("cuota", "5"))
                .andExpect(flash().attribute("error", containsString("100 caracteres")));
    }

    // --- N-16: nota NaN ---

    @Test
    void unaNotaNaNSeRechaza() throws Exception {
        var getafe = equipos.findByNombre("Getafe CF").orElseThrow();
        Jugador jugador = jugadores.save(new Jugador(getafe, "Prueba", Posicion.PORTERO));

        mvc.perform(post("/gestion/jugadores/{id}/nota", jugador.getId()).with(CREADOR).with(csrf()).param("nota", "NaN"))
                .andExpect(flash().attribute("error", containsString("entre 0 y 10")));
        assertThat(jugador.getNota()).isEqualTo(Jugador.NOTA_INICIAL);
    }

    // --- N-20: solo se marcan como leídos los avisos que se ven ---

    @Test
    void soloSeMarcanLeidosLosAvisosQueSeMuestran() {
        Usuario ana = usuarios.findByEmail(ANA).orElseThrow();
        for (int i = 0; i < 55; i++) {
            notificaciones.avisar(ana, "GANADA", "Aviso " + i);
        }

        notificaciones.leer(ANA);

        assertThat(notificaciones.sinLeer(ANA)).isEqualTo(5);
    }

    // --- F-08: bloqueo tras varios intentos fallidos ---

    @Test
    void trasCincoFallosNoSePuedeEntrarAunqueSeAcierte() throws Exception {
        String email = "probador@ucm.es";
        jugador(email);
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/login").with(csrf()).param("email", email).param("password", "mala-" + i))
                    .andExpect(redirectedUrl("/login?error"));
        }

        mvc.perform(post("/login").with(csrf()).param("email", email).param("password", "secreta123"))
                .andExpect(redirectedUrl("/login?error"));
        mvc.perform(post("/login").with(csrf()).param("email", ANA).param("password", "secreta123"))
                .andExpect(redirectedUrlPattern("/**"));
    }
}

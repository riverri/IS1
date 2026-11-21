package es.ucm.fdi.is1.apuestas.fantasy;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.JugadorRepository;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.fantasy.FantasyService.PuestoFantasy;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-81 a HU-83: la liga fantasy. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FantasyWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final String LUIS = "luis@ucm.es";
    private static final String JEFA = "jefa@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");
    /**
     * Las pruebas de puntos van en una semana sin más partidos que los suyos (los de prueba empiezan mañana): un
     * miércoles. Así no dependen de qué día se pasen (ver FantasyService.Jornada).
     */
    private static final LocalDateTime LUNES = LocalDateTime.of(2031, 1, 6, 0, 0);
    private static final LocalDateTime MIERCOLES = LUNES.plusDays(2).withHour(12);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FantasyService fantasy;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private JugadorRepository jugadores;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private jakarta.persistence.EntityManager em;

    @Autowired
    private es.ucm.fdi.is1.apuestas.apuesta.BajaService bajas;

    private Equipo getafe;
    private Equipo sevilla;
    private Jugador portero;
    private Jugador defensa;
    private Jugador delantero;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        usuarioService.crear(JEFA, "Jefa", "secreta123", Rol.CREADOR);
        getafe = equipos.findByNombre("Getafe CF").orElseThrow();
        sevilla = equipos.findByNombre("Sevilla FC").orElseThrow();
        portero = jugador(getafe, "Portero Getafe", Posicion.PORTERO, 6);
        defensa = jugador(getafe, "Defensa Getafe", Posicion.DEFENSA, 6);
        delantero = jugador(sevilla, "Delantero Sevilla", Posicion.DELANTERO, 7);
    }

    private Jugador jugador(Equipo equipo, String nombre, Posicion posicion, double nota) {
        Jugador jugador = new Jugador(equipo, nombre, posicion);
        jugador.cambiarNota(nota);
        return jugadores.save(jugador);
    }

    private Evento partido(Equipo local, Equipo visitante, long horas) {
        return partidoEl(local, visitante, Hora.ahora().plusHours(horas));
    }

    private Evento partidoEl(Equipo local, Equipo visitante, LocalDateTime cuando) {
        return eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(), local, visitante, cuando));
    }

    @Test
    void creaSuEquipoYFichaConElPresupuesto() throws Exception {
        // CA-81.1
        mvc.perform(get("/fantasy").with(COMO_ANA)).andExpect(content().string(containsString("Crea tu equipo")));
        mvc.perform(post("/fantasy/crear").with(COMO_ANA).with(csrf()).param("nombre", "Los del Aula 3"))
                .andExpect(redirectedUrl("/fantasy"));

        mvc.perform(post("/fantasy/fichar").with(COMO_ANA).with(csrf())
                        .param("jugador", portero.getId().toString()).param("posicion", "PORTERO"))
                .andExpect(redirectedUrl("/fantasy/mercado?posicion=PORTERO"))
                .andExpect(flash().attribute("mensaje", containsString("Has fichado a Portero Getafe.")));

        EquipoFantasy equipo = fantasy.equipoDe(ANA).orElseThrow();
        // Precio: nota 6 × 1,5 + calidad del Getafe × 0,5
        BigDecimal precio = EquipoFantasy.precio(portero);
        assertThat(precio).isEqualByComparingTo(BigDecimal.valueOf(9 + getafe.getCalidad() * 0.5)
                .setScale(1, java.math.RoundingMode.HALF_UP));
        assertThat(equipo.getCaja()).isEqualByComparingTo(EquipoFantasy.PRESUPUESTO.subtract(precio));
        mvc.perform(get("/fantasy").with(COMO_ANA))
                .andExpect(content().string(containsString("Portero Getafe")))
                .andExpect(content().string(containsString("1/7")));
        // Es gratis: no cambia el saldo
        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo()).isEqualByComparingTo("1000");
    }

    @Test
    void respetaLaFormacionElMaximoPorClubYElPresupuesto() {
        // CA-81.2
        fantasy.crear(ANA, "Los del Aula 3");
        fantasy.fichar(ANA, portero.getId());
        Jugador otroPortero = jugador(sevilla, "Portero Sevilla", Posicion.PORTERO, 5);
        assertThatThrownBy(() -> fantasy.fichar(ANA, otroPortero.getId()))
                .hasMessage("No te quedan huecos de portero");
        assertThatThrownBy(() -> fantasy.fichar(ANA, portero.getId())).hasMessage("Ya está en tu equipo");

        fantasy.fichar(ANA, defensa.getId());
        Jugador medio = jugador(getafe, "Medio Getafe", Posicion.CENTROCAMPISTA, 6);
        assertThatThrownBy(() -> fantasy.fichar(ANA, medio.getId())).hasMessage("Ya tienes 2 del Getafe CF");

        // Getafe: 2 × 12,1 = 24,2. Dos medios del Liverpool (nota 10): 2 × 19,5. Un delantero del Arsenal: 19,6
        Equipo liverpool = equipos.findByNombre("Liverpool").orElseThrow();
        Equipo arsenal = equipos.findByNombre("Arsenal").orElseThrow();
        fantasy.fichar(ANA, jugador(liverpool, "Medio 1", Posicion.CENTROCAMPISTA, 10).getId());
        fantasy.fichar(ANA, jugador(liverpool, "Medio 2", Posicion.CENTROCAMPISTA, 10).getId());
        fantasy.fichar(ANA, jugador(arsenal, "Delantero 1", Posicion.DELANTERO, 10).getId());
        EquipoFantasy equipo = fantasy.equipoDe(ANA).orElseThrow();
        assertThat(equipo.getCaja()).isEqualByComparingTo("7.2");
        Jugador caro = jugador(arsenal, "Delantero 2", Posicion.DELANTERO, 10);
        assertThatThrownBy(() -> fantasy.fichar(ANA, caro.getId())).hasMessage("No te llega el presupuesto");
        // Uno barato sí entra (nota 2 en el Sevilla: 3 + 3,4 = 6,4)
        fantasy.fichar(ANA, jugador(sevilla, "Delantero barato", Posicion.DELANTERO, 2).getId());
        assertThat(fantasy.equipoDe(ANA).orElseThrow().getCaja()).isEqualByComparingTo("0.8");
    }

    @Test
    void soloCuentanLosPartidosDespuesDeFicharYLoVendidoSeQueda() {
        // CA-82.1: el portero del Getafe, fichado el miércoles; el partido del domingo anterior no le cuenta
        Evento yaEmpezado = partidoEl(getafe, sevilla, LUNES.minusHours(3));
        Evento siguiente = partidoEl(sevilla, getafe, MIERCOLES.plusDays(1));
        fantasy.crear(ANA, "Los del Aula 3");
        fantasy.fichar(ANA, portero.getId(), MIERCOLES);
        fantasy.fichar(ANA, delantero.getId(), MIERCOLES);

        resolucion.introducirMarcador(yaEmpezado.getId(), 5, 0);
        resolucion.introducirMarcador(siguiente.getId(), 0, 1);  // gana el Getafe 0-1 fuera, portería a cero

        EquipoFantasy equipo = fantasy.equipoDe(ANA).orElseThrow();
        int total = fantasy.puntos(equipo).stream().mapToInt(FantasyService.PuntosFichaje::getTotal).sum();
        // Portero del Getafe: victoria 3 + portería a cero 4 = 7; delantero del Sevilla: derrota sin goles = 0
        assertThat(total).isEqualTo(7);

        fantasy.vender(ANA, portero.getId(), MIERCOLES.plusDays(2));
        equipo = fantasy.equipoDe(ANA).orElseThrow();
        assertThat(equipo.getPlantilla()).extracting(f -> f.getJugador().getNombre())
                .containsExactly("Delantero Sevilla");
        assertThat(fantasy.puntos(equipo).stream().mapToInt(FantasyService.PuntosFichaje::getTotal).sum())
                .isEqualTo(7);
    }

    @Test
    void laClasificacionVaDeMasAMenosPuntos() throws Exception {
        // HU-83
        Evento partido = partidoEl(getafe, sevilla, MIERCOLES.plusHours(2));
        fantasy.crear(ANA, "Los del Aula 3");
        fantasy.crear(LUIS, "Luis FC");
        fantasy.fichar(ANA, delantero.getId(), MIERCOLES);
        fantasy.fichar(LUIS, portero.getId(), MIERCOLES);
        resolucion.introducirMarcador(partido.getId(), 3, 1);

        // Luis (portero del Getafe que gana 3-1): 3; Ana (delantero del Sevilla que pierde, 1 gol): 2
        List<PuestoFantasy> clasificacion = fantasy.clasificacion(ANA);
        assertThat(clasificacion).extracting(p -> p.equipo().getNombre()).containsExactly("Luis FC", "Los del Aula 3");
        assertThat(clasificacion).extracting(PuestoFantasy::puntos).containsExactly(3, 2);
        assertThat(clasificacion.get(1).mio()).isTrue();
        mvc.perform(get("/fantasy").with(COMO_ANA)).andExpect(content().string(containsString("Luis FC")));
    }

    @Test
    void elMercadoFiltraPorPosicionYDiceQuienNoSePuedeFichar() throws Exception {
        fantasy.crear(ANA, "Los del Aula 3");
        fantasy.fichar(ANA, portero.getId());
        mvc.perform(get("/fantasy/mercado").with(COMO_ANA).param("posicion", "DEFENSA"))
                .andExpect(content().string(containsString("Defensa Getafe")))
                .andExpect(content().string(not(containsString("Delantero Sevilla"))));
        Jugador otroPortero = jugador(sevilla, "Portero Sevilla", Posicion.PORTERO, 5);
        mvc.perform(get("/fantasy/mercado").with(COMO_ANA).param("posicion", "PORTERO"))
                .andExpect(content().string(containsString(otroPortero.getNombre())))
                .andExpect(content().string(containsString("No te quedan huecos de portero")));
    }

    @Test
    void sinEquipoElMercadoLlevaACrearloYElCreadorNoJuega() throws Exception {
        mvc.perform(get("/fantasy/mercado").with(COMO_ANA)).andExpect(redirectedUrl("/fantasy"));
        assertThatThrownBy(() -> fantasy.crear(JEFA, "La casa")).isInstanceOf(IllegalArgumentException.class);
        fantasy.crear(ANA, "Uno");
        assertThatThrownBy(() -> fantasy.crear(ANA, "Otro")).isInstanceOf(IllegalStateException.class);
    }

    // --- Cuarta depuración ---

    private int puntosDe(String email) {
        return fantasy.puntos(fantasy.equipoDe(email).orElseThrow()).stream()
                .mapToInt(FantasyService.PuntosFichaje::getTotal).sum();
    }

    @Test
    void unTraspasoNoCambiaLoQueYaSumoNiDesdeQueClubPuntua() {
        Evento partido = partidoEl(getafe, sevilla, MIERCOLES.plusHours(2));
        fantasy.crear(ANA, "Los del Aula 3");
        fantasy.fichar(ANA, portero.getId(), MIERCOLES);
        resolucion.introducirMarcador(partido.getId(), 2, 0);
        assertThat(puntosDe(ANA)).isEqualTo(7);

        // La API lo traspasa al Sevilla y le cambia la posición: lo sumado con el Getafe se queda
        portero.actualizar(sevilla, "Portero Getafe", Posicion.DELANTERO, null, null, null);
        assertThat(puntosDe(ANA)).isEqualTo(7);
        EquipoFantasy equipo = fantasy.equipoDe(ANA).orElseThrow();
        assertThat(equipo.huecos(Posicion.PORTERO)).as("sigue ocupando su hueco de portero").isZero();
        assertThat(equipo.huecos(Posicion.DELANTERO)).isEqualTo(EquipoFantasy.FORMACION.get(Posicion.DELANTERO));
    }

    @Test
    void borrarUnJugadorNoBorraSusPuntosYSePuedeVenderSuFichaje() {
        Evento partido = partidoEl(getafe, sevilla, MIERCOLES.plusHours(2));
        fantasy.crear(ANA, "Los del Aula 3");
        FichajeFantasy fichaje = fantasy.fichar(ANA, portero.getId(), MIERCOLES);
        resolucion.introducirMarcador(partido.getId(), 2, 0);
        em.flush();
        em.clear();

        jugadores.delete(jugadores.findById(portero.getId()).orElseThrow());
        em.flush();
        em.clear();

        assertThat(puntosDe(ANA)).isEqualTo(7);
        EquipoFantasy equipo = fantasy.equipoDe(ANA).orElseThrow();
        assertThat(equipo.getPlantilla()).singleElement().satisfies(f -> {
            assertThat(f.getJugador()).isNull();
            assertThat(f.getNombreJugador()).isEqualTo("Portero Getafe");
        });
        fantasy.venderFichaje(ANA, fichaje.getId(), MIERCOLES.plusDays(1));
        assertThat(fantasy.equipoDe(ANA).orElseThrow().getPlantilla()).isEmpty();
        assertThat(puntosDe(ANA)).isEqualTo(7);
    }

    @Test
    void lasCuentasEliminadasNoSalenEnLaClasificacion() {
        fantasy.crear(ANA, "Los del Aula 3");
        fantasy.crear(LUIS, "Luis FC");
        bajas.eliminarCuenta(LUIS, "secreta123");
        assertThat(fantasy.clasificacion(ANA)).extracting(p -> p.equipo().getNombre()).containsExactly("Los del Aula 3");
    }

    @Test
    void venderPorElFichajeDesdeLaPagina() throws Exception {
        fantasy.crear(ANA, "Los del Aula 3");
        FichajeFantasy fichaje = fantasy.fichar(ANA, portero.getId());
        mvc.perform(get("/fantasy").with(COMO_ANA))
                .andExpect(content().string(containsString("name=\"fichaje\" value=\"" + fichaje.getId() + "\"")));
        mvc.perform(post("/fantasy/vender").with(COMO_ANA).with(csrf()).param("fichaje", fichaje.getId().toString()))
                .andExpect(flash().attribute("mensaje", containsString("Has vendido a Portero Getafe")));
        assertThat(fantasy.equipoDe(ANA).orElseThrow().getPlantilla()).isEmpty();
    }

    @Test
    void conLaJornadaEmpezadaLoQueSeFichaSumaDesdeElLunes() {
        // La jornada empieza con su primer partido (el suspendido del lunes no cuenta): el miércoles a las 12
        resolucion.suspender(partidoEl(sevilla, getafe, LUNES.plusHours(20)).getId());
        Evento miercoles = partidoEl(getafe, sevilla, MIERCOLES);
        Evento sabado = partidoEl(sevilla, getafe, MIERCOLES.plusDays(3));
        Evento martesSiguiente = partidoEl(getafe, sevilla, MIERCOLES.plusDays(6));
        FantasyService.Jornada jornada = fantasy.jornada(MIERCOLES.minusMinutes(1));
        assertThat(jornada.plazo()).isEqualTo(MIERCOLES);
        assertThat(jornada.empezada()).isFalse();
        assertThat(fantasy.jornada(MIERCOLES).empezada()).isTrue();
        assertThat(fantasy.jornada(MIERCOLES).fin()).isEqualTo(LUNES.plusWeeks(1));

        fantasy.crear(ANA, "Los del Aula 3");
        FichajeFantasy fichaje = fantasy.fichar(ANA, portero.getId(), MIERCOLES.plusDays(1));
        assertThat(fichaje.getDesde()).isEqualTo(LUNES.plusWeeks(1));
        assertThat(fantasy.equipoDe(ANA).orElseThrow().getPlantilla()).as("ya ocupa su hueco").hasSize(1);

        resolucion.introducirMarcador(miercoles.getId(), 1, 0);
        resolucion.introducirMarcador(sabado.getId(), 0, 2);
        resolucion.introducirMarcador(martesSiguiente.getId(), 0, 0);
        // Solo el martes siguiente: empate 1 + portería a cero 4
        assertThat(puntosDe(ANA)).isEqualTo(5);
    }

    @Test
    void unHuecoNoSumaDosPartidosDeLaMismaJornada() {
        // Antes había truco: vender al portero del Getafe tras ganar el miércoles y fichar al del Sevilla, que
        // gana el sábado, para sumar las dos victorias en el mismo hueco
        Jugador porteroSevilla = jugador(sevilla, "Portero Sevilla", Posicion.PORTERO, 6);
        Equipo betis = equipos.findByNombre("Real Betis").orElseThrow();
        Evento miercoles = partidoEl(getafe, betis, MIERCOLES);
        Evento sabado = partidoEl(sevilla, betis, MIERCOLES.plusDays(3));
        Evento domingo = partidoEl(betis, getafe, MIERCOLES.plusDays(4));
        fantasy.crear(ANA, "Los del Aula 3");
        fantasy.fichar(ANA, portero.getId(), LUNES.plusHours(10));
        resolucion.introducirMarcador(miercoles.getId(), 1, 0);

        FichajeFantasy vendido = fantasy.vender(ANA, portero.getId(), MIERCOLES.plusDays(1));
        fantasy.fichar(ANA, porteroSevilla.getId(), MIERCOLES.plusDays(1));
        assertThat(vendido.isActivo()).as("deja el hueco libre").isFalse();
        assertThat(vendido.getHasta()).isEqualTo(LUNES.plusWeeks(1));
        assertThat(fantasy.equipoDe(ANA).orElseThrow().getPlantilla()).extracting(FichajeFantasy::getNombreJugador)
                .containsExactly("Portero Sevilla");

        resolucion.introducirMarcador(sabado.getId(), 2, 0);
        resolucion.introducirMarcador(domingo.getId(), 3, 0);
        // El del Getafe sigue sumando lo que queda de jornada: 7 del miércoles y −1 del domingo (encaja 3).
        // El del Sevilla no suma el sábado: empieza el lunes
        assertThat(puntosDe(ANA)).isEqualTo(6);
    }

    @Test
    void laPaginaAvisaDeQueLaJornadaHaEmpezado() throws Exception {
        partido(getafe, sevilla, 0);
        fantasy.crear(ANA, "Los del Aula 3");
        mvc.perform(post("/fantasy/fichar").with(COMO_ANA).with(csrf()).param("jugador", portero.getId().toString()))
                .andExpect(flash().attribute("mensaje", containsString("suma desde el lunes")));
        mvc.perform(get("/fantasy").with(COMO_ANA))
                .andExpect(content().string(containsString("La jornada ya ha empezado")))
                .andExpect(content().string(containsString("Suma desde el lunes")));
        mvc.perform(get("/fantasy/mercado").with(COMO_ANA))
                .andExpect(content().string(containsString("La jornada ya ha empezado")));
        mvc.perform(post("/fantasy/vender").with(COMO_ANA).with(csrf()).param("jugador", portero.getId().toString()))
                .andExpect(flash().attribute("mensaje", containsString("sigue sumando hasta que acabe")));
    }
}

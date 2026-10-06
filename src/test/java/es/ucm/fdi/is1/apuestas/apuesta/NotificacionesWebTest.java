package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-37: avisos al resolverse una apuesta. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificacionesWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private NotificacionService notificaciones;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento partido;
    private Evento otro;

    @BeforeEach
    void preparar() {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        otro = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(),
                equipos.findByNombre("Levante UD").orElseThrow(), Hora.ahora().plusDays(2)));
    }

    @Test
    void alGanarRecibeUnAvisoConLoQueCobra() {
        Apuesta apuesta = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));

        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);

        assertThat(notificaciones.sinLeer(ANA)).isEqualTo(1);
        List<NotificacionService.Aviso> avisos = notificaciones.leer(ANA);
        assertThat(avisos.get(0).tipo()).isEqualTo("ganada");
        assertThat(avisos.get(0).texto()).contains("Getafe CF")
                .contains(apuesta.getGananciaPotencial().toPlainString().replace('.', ','));
        assertThat(notificaciones.sinLeer(ANA)).isZero();
    }

    @Test
    void unaCombinadaPendienteDeOtroPartidoNoAvisa() {
        apuestas.apostar(ANA, List.of(new SeleccionPedida(partido.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(otro.getId(), Resultado.LOCAL, null)), new BigDecimal("10"));

        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);
        assertThat(notificaciones.sinLeer(ANA)).isZero();

        resolucion.introducirResultado(otro.getId(), Resultado.VISITANTE);
        List<NotificacionService.Aviso> avisos = notificaciones.leer(ANA);
        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).texto()).isEqualTo("Has perdido tu combinada de 2 selecciones.");
    }

    @Test
    void anularYCorregirTambienAvisan() {
        apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));
        resolucion.introducirResultado(partido.getId(), Resultado.VISITANTE);
        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);

        apuestas.apostar(ANA, otro.getId(), Resultado.LOCAL, new BigDecimal("5"));
        resolucion.anular(otro.getId());

        List<NotificacionService.Aviso> avisos = notificaciones.leer(ANA);
        assertThat(avisos).extracting(NotificacionService.Aviso::texto).anyMatch(t -> t.startsWith("Resultado corregido."))
                .anyMatch(t -> t.contains("Te hemos devuelto 5,00 monedas"));
        assertThat(avisos).hasSize(3);
    }

    @Test
    void laCampanaMuestraLosAvisosSinLeerYAlAbrirlosSeLeen() throws Exception {
        apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));
        resolucion.introducirResultado(partido.getId(), Resultado.VISITANTE);

        mvc.perform(get("/eventos").with(COMO_ANA))
                .andExpect(content().string(containsString("1 avisos sin leer")));
        mvc.perform(get("/notificaciones").with(COMO_ANA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Has perdido tu apuesta a Getafe CF")))
                .andExpect(content().string(containsString("Nuevo")));
        mvc.perform(get("/eventos").with(COMO_ANA))
                .andExpect(content().string(not(containsString("avisos sin leer"))));
    }

    @Test
    void sinSesionNoHayAvisos() throws Exception {
        mvc.perform(get("/notificaciones")).andExpect(status().is3xxRedirection());
    }
}

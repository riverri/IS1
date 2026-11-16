package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * HU-77: correo al ganar una apuesta. La prueba es transaccional y nunca se guarda, así que el correo no llega a
 * salir: se comprueba el evento que lo pide. {@link CorreoTrasGuardarTest} comprueba que sale al guardar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RecordApplicationEvents
class CorreoPremioWebTest {

    private static final String ANA = "ana-correo@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationEvents eventosPublicados;

    @MockitoBean
    private EnvioPremio envio;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private NotificacionService notificaciones;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento partido;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
    }

    private List<PremioGanado> premios() {
        return eventosPublicados.stream(PremioGanado.class).toList();
    }

    @Test
    void alGanarSePideUnCorreoConElTextoDelAviso() {
        apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));

        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);

        String aviso = notificaciones.leer(ANA).get(0).texto();
        assertThat(premios()).containsExactly(new PremioGanado(ANA, "Ana", aviso));
        // Todavía no se ha guardado la transacción: el correo espera
        verify(envio, never()).enviarPremio(any(), any(), any(), any());
    }

    @Test
    void alPerderNoHayCorreo() {
        apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));

        resolucion.introducirResultado(partido.getId(), Resultado.VISITANTE);

        assertThat(premios()).isEmpty();
    }

    @Test
    void conLosCorreosDesactivadosSoloQuedaElAvisoDeLaWeb() {
        usuarioService.activarCorreos(ANA, false);
        apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));

        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);

        assertThat(premios()).isEmpty();
        assertThat(notificaciones.sinLeer(ANA)).isEqualTo(1);
    }

    @Test
    void alCorregirElResultadoNoSeMandaCorreo() {
        apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));
        resolucion.introducirResultado(partido.getId(), Resultado.VISITANTE);

        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);

        assertThat(premios()).isEmpty();
    }

    @Test
    void losCorreosSeDesactivanYActivanDesdeMiCuenta() throws Exception {
        mvc.perform(get("/cuenta").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Desactivar los correos")));

        mvc.perform(post("/cuenta/correos").param("activados", "false").with(user(ANA)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(usuarios.findByEmail(ANA).orElseThrow().isCorreosActivados()).isFalse();
        mvc.perform(get("/cuenta").with(user(ANA)))
                .andExpect(content().string(containsString("Activar los correos")));

        mvc.perform(post("/cuenta/correos").param("activados", "true").with(user(ANA)).with(csrf()));
        assertThat(usuarios.findByEmail(ANA).orElseThrow().isCorreosActivados()).isTrue();
    }
}

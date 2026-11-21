package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-92: resumen semanal por correo. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResumenSemanalTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ResumenSemanalService resumen;

    @MockitoBean
    private EnvioResumen envio;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private TareaRealizadaRepository tareas;

    @Test
    void llegaAQuienHaJugadoConLoJugadoLoCobradoYSuPuesto() throws Exception {
        usuarios.crear("ana-resumen@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        usuarios.crear("bea-resumen@ucm.es", "Bea", "secreta123", Rol.USUARIO);
        usuarios.crear("carla-resumen@ucm.es", "Carla", "secreta123", Rol.USUARIO);
        Evento partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        Apuesta ganada = apuestas.apostar("ana-resumen@ucm.es", partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        apuestas.apostar("carla-resumen@ucm.es", partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);
        // Carla lo desactiva desde Mi cuenta
        mvc.perform(post("/cuenta/resumen").param("activado", "false").with(user("carla-resumen@ucm.es")).with(csrf()));
        assertThat(usuarioRepository.findByEmail("carla-resumen@ucm.es").orElseThrow().isResumenSemanal()).isFalse();

        resumen.enviar();

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        verify(envio).enviarResumen(eq("ana-resumen@ucm.es"), eq("Ana"), texto.capture());
        String cobrado = ganada.getGananciaPotencial().toPlainString().replace('.', ',');
        assertThat(texto.getValue()).contains("Has jugado 10,00 monedas").contains("Has cobrado " + cobrado)
                .contains("º de ").contains("/apuestas");
        // Bea no ha jugado esta semana y Carla lo ha desactivado
        verify(envio, never()).enviarResumen(eq("bea-resumen@ucm.es"), any(), any());
        verify(envio, never()).enviarResumen(eq("carla-resumen@ucm.es"), any(), any());
    }

    @Test
    void elUltimoLunesALasNueve() {
        LocalDateTime lunes = LocalDateTime.of(2026, 11, 23, 9, 0);
        assertThat(ResumenSemanalService.ultimoLunes(lunes)).isEqualTo(lunes);
        assertThat(ResumenSemanalService.ultimoLunes(lunes.minusMinutes(1))).as("lunes a las 8:59").isEqualTo(lunes.minusWeeks(1));
        assertThat(ResumenSemanalService.ultimoLunes(LocalDateTime.of(2026, 11, 26, 18, 30))).isEqualTo(lunes);
        assertThat(ResumenSemanalService.ultimoLunes(LocalDateTime.of(2026, 11, 29, 23, 59))).as("domingo").isEqualTo(lunes);
    }

    @Test
    void laPrimeraVezSoloApuntaYSiSePerdioUnLunesLoMandaAlVolver() {
        usuarios.crear("dani-resumen@ucm.es", "Dani", "secreta123", Rol.USUARIO);
        Evento partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        apuestas.apostar("dani-resumen@ucm.es", partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        tareas.deleteById(ResumenSemanalService.TAREA);

        // Primera vez con la tabla: no sabe si la versión anterior ya lo mandó, así que solo apunta el último lunes
        assertThat(resumen.enviarSiToca()).isZero();
        LocalDateTime apuntado = tareas.findById(ResumenSemanalService.TAREA).orElseThrow().getUltima();
        assertThat(apuntado).isEqualTo(ResumenSemanalService.ultimoLunes(Hora.ahora()));
        verify(envio, never()).enviarResumen(any(), any(), any());

        // La aplicación estaba parada el último lunes: lo último apuntado es de la semana anterior
        tareas.findById(ResumenSemanalService.TAREA).orElseThrow().marcar(apuntado.minusWeeks(1));
        assertThat(resumen.enviarSiToca()).isPositive();
        verify(envio).enviarResumen(eq("dani-resumen@ucm.es"), eq("Dani"), any());
        assertThat(tareas.findById(ResumenSemanalService.TAREA).orElseThrow().getUltima()).isEqualTo(apuntado);

        // Una hora después ya no toca: no se repite
        assertThat(resumen.enviarSiToca()).isZero();
        verify(envio, times(1)).enviarResumen(eq("dani-resumen@ucm.es"), any(), any());
    }
}

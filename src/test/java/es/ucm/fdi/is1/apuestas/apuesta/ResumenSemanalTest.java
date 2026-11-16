package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;

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
}

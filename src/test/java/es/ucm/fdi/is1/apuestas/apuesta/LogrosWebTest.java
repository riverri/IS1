package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-88: logros del perfil público. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LogrosWebTest {

    private static final String ANA = "ana-logros@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private LogrosService logros;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento partido(int dias) {
        return eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(dias)));
    }

    private Map<String, Boolean> conseguidos(Long id) {
        return logros.de(id).stream().collect(Collectors.toMap(LogrosService.Logro::nombre, LogrosService.Logro::conseguido));
    }

    @Test
    void losLogrosAparecenEnCuantoSeCumplen() throws Exception {
        Usuario ana = usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        assertThat(conseguidos(ana.getId())).doesNotContainValue(true).hasSize(8);

        // Cinco simples ganadas seguidas y una combinada ganada
        for (int i = 1; i <= 5; i++) {
            Evento e = partido(i);
            apuestas.apostar(ANA, e.getId(), Resultado.LOCAL, BigDecimal.ONE);
            resolucion.introducirResultado(e.getId(), Resultado.LOCAL);
        }
        Evento a = partido(6);
        Evento b = partido(7);
        apuestas.apostar(ANA, List.of(new SeleccionPedida(a.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(b.getId(), Resultado.LOCAL, null)), BigDecimal.ONE);
        resolucion.introducirResultado(a.getId(), Resultado.LOCAL);
        resolucion.introducirResultado(b.getId(), Resultado.LOCAL);

        Map<String, Boolean> ahora = conseguidos(ana.getId());
        assertThat(ahora).containsEntry("Primera apuesta", true).containsEntry("Primer acierto", true)
                .containsEntry("Combinada ganadora", true).containsEntry("Racha de 5", true)
                .containsEntry("Quiniela ganadora", false).containsEntry("Visionario", false)
                // Solo 6 resueltas: hacen falta 10
                .containsEntry("En positivo", false);

        // La combinada puede pasar de cuota 5 («Cuota alta»): la página cuenta los que haya
        long cuantos = ahora.values().stream().filter(Boolean::booleanValue).count();
        mvc.perform(get("/jugadores/{id}", ana.getId()))
                .andExpect(content().string(containsString("Logros · " + cuantos + " de 8")))
                .andExpect(content().string(containsString("Racha de 5")));
    }

    @Test
    void unaPerdidaCortaLaRacha() {
        Usuario ana = usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        for (int i = 1; i <= 6; i++) {
            Evento e = partido(i);
            apuestas.apostar(ANA, e.getId(), Resultado.LOCAL, BigDecimal.ONE);
            resolucion.introducirResultado(e.getId(), i == 3 ? Resultado.VISITANTE : Resultado.LOCAL);
        }
        assertThat(conseguidos(ana.getId())).containsEntry("Racha de 5", false);
    }
}

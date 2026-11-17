package es.ucm.fdi.is1.apuestas.seguimiento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.cuotas.CuotaHistoricaRepository;
import es.ucm.fdi.is1.apuestas.cuotas.RegistroCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-91: evolución de la cuota de cada resultado. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EvolucionCuotaWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RegistroCuotas registro;

    @Autowired
    private es.ucm.fdi.is1.apuestas.gestion.GestionService gestion;

    @Autowired
    private CuotaHistoricaRepository historico;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @Test
    void cadaCambioDeCuotaQuedaApuntadoYSeDibuja() throws Exception {
        usuarios.crear("ana-evolucion@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        Evento partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));

        // Con un solo valor todavía no hay línea
        mvc.perform(get("/eventos/{id}/apostar", partido.getId()).with(user("ana-evolucion@ucm.es")))
                .andExpect(content().string(not(containsString("Evolución de la cuota"))));
        assertThat(historico.findByEventoOrderByFechaAscIdAsc(partido)).hasSize(1);
        // Sin cambios no se apunta otro igual
        registro.registrar(partido);
        assertThat(historico.findByEventoOrderByFechaAscIdAsc(partido)).hasSize(1);

        apuestas.apostar("ana-evolucion@ucm.es", partido.getId(), Resultado.LOCAL, new BigDecimal("100"));

        assertThat(historico.findByEventoOrderByFechaAscIdAsc(partido)).hasSize(2);
        RegistroCuotas.Evolucion evolucion = registro.evolucion(partido);
        assertThat(evolucion.ultimas().local()).isLessThan(evolucion.primeras().local());
        mvc.perform(get("/eventos/{id}/apostar", partido.getId()).with(user("ana-evolucion@ucm.es")))
                .andExpect(content().string(containsString("Evolución de la cuota")))
                .andExpect(content().string(containsString("linea-empate")));
    }

    @Test
    void unEventoSinApuestasSeBorraConSuHistorial() {
        Evento partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(),
                equipos.findByNombre("Levante UD").orElseThrow(), Hora.ahora().plusDays(1)));
        registro.registrar(partido);

        gestion.borrarEvento(partido.getId());

        assertThat(eventos.findById(partido.getId())).isEmpty();
        assertThat(historico.findAll()).noneMatch(c -> c.getEvento().getId().equals(partido.getId()));
    }
}

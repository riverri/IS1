package es.ucm.fdi.is1.apuestas.gestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.Apuesta;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.apuesta.BeneficioDiarioService;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-95: beneficio de la casa en cada uno de los últimos 30 días. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BeneficioDiarioWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BeneficioDiarioService diario;

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

    private BigDecimal hoy() {
        LocalDate hoy = Hora.ahora().toLocalDate();
        return diario.ultimosDias().dias().stream().filter(d -> d.fecha().equals(hoy)).findFirst().orElseThrow().beneficio();
    }

    @Test
    void loQuePierdenLosJugadoresLoGanaLaCasaYLosPremiosLoRestan() throws Exception {
        usuarios.crear("ana-diario@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        usuarios.crear("luis-diario@ucm.es", "Luis", "secreta123", Rol.USUARIO);
        Evento partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        BigDecimal antes = hoy();

        apuestas.apostar("ana-diario@ucm.es", partido.getId(), Resultado.VISITANTE, new BigDecimal("40"));
        Apuesta ganadora = apuestas.apostar("luis-diario@ucm.es", partido.getId(), Resultado.LOCAL, new BigDecimal("10"));
        // El saldo de bienvenida de los dos no cuenta: no es un juego
        assertThat(hoy().subtract(antes)).isEqualByComparingTo("50");

        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);

        assertThat(hoy().subtract(antes)).isEqualByComparingTo(new BigDecimal("50").subtract(ganadora.getGananciaPotencial()));
        assertThat(diario.ultimosDias().dias()).hasSize(30);
        mvc.perform(get("/gestion/casa").with(user("creador@apuestas.es").roles("CREADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Últimos 30 días")))
                .andExpect(content().string(containsString("Beneficio de cada día")));
    }
}

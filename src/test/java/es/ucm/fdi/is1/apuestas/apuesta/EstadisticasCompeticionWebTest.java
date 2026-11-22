package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.math.BigDecimal;
import java.util.List;

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
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-96: mis estadísticas por competición en Mis apuestas. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EstadisticasCompeticionWebTest {

    private static final String ANA = "ana-competicion@ucm.es";

    @Autowired
    private MockMvc mvc;

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

    private Evento partido(String competicion, String local, String visitante) {
        return eventos.save(new Evento(competiciones.findByNombre(competicion).orElseThrow(),
                equipos.findByNombre(local).orElseThrow(), equipos.findByNombre(visitante).orElseThrow(),
                Hora.ahora().plusDays(1)));
    }

    @Test
    void cadaCompeticionTieneSusAciertosYSuBalanceYLasCombinadasVanAparte() throws Exception {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        Evento liga1 = partido("LaLiga", "Getafe CF", "Sevilla FC");
        Evento liga2 = partido("LaLiga", "Elche CF", "Levante UD");
        Evento basket = partido("Liga ACB", "Unicaja", "Baskonia");
        Apuesta ganada = apuestas.apostar(ANA, liga1.getId(), Resultado.LOCAL, BigDecimal.TEN);
        apuestas.apostar(ANA, liga2.getId(), Resultado.LOCAL, BigDecimal.TEN);
        apuestas.apostar(ANA, basket.getId(), Resultado.LOCAL, new BigDecimal("5"));
        apuestas.apostar(ANA, List.of(new SeleccionPedida(liga1.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(basket.getId(), Resultado.LOCAL, null)), new BigDecimal("2"));
        // Sigue activa: no cuenta
        apuestas.apostar(ANA, partido("LaLiga", "Real Madrid", "Elche CF").getId(), Resultado.LOCAL, BigDecimal.ONE);

        resolucion.introducirResultado(liga1.getId(), Resultado.LOCAL);
        resolucion.introducirResultado(liga2.getId(), Resultado.VISITANTE);
        resolucion.introducirResultado(basket.getId(), Resultado.VISITANTE);

        List<ApuestaService.EstadisticasGrupo> grupos = apuestas.estadisticasPorCompeticion(ANA);
        Estadisticas agregadas = apuestas.estadisticas(ANA);
        Estadisticas historicoCompleto = Estadisticas.de(apuestas.historial(ANA));
        assertThat(agregadas.resueltas()).isEqualTo(historicoCompleto.resueltas());
        assertThat(agregadas.ganadas()).isEqualTo(historicoCompleto.ganadas());
        assertThat(agregadas.apostado()).isEqualByComparingTo(historicoCompleto.apostado());
        assertThat(agregadas.beneficio()).isEqualByComparingTo(historicoCompleto.beneficio());
        assertThat(grupos).extracting(ApuestaService.EstadisticasGrupo::nombre)
                .containsExactly("LaLiga", "Combinadas", "Liga ACB");
        Estadisticas laLiga = grupos.get(0).estadisticas();
        assertThat(laLiga.resueltas()).isEqualTo(2);
        assertThat(laLiga.getPorcentajeAciertos()).isEqualByComparingTo("50");
        assertThat(laLiga.beneficio()).isEqualByComparingTo(ganada.getGananciaPotencial().subtract(new BigDecimal("20")));
        assertThat(grupos.get(2).estadisticas().beneficio()).isEqualByComparingTo("-5");

        mvc.perform(get("/apuestas").with(user(ANA)))
                .andExpect(content().string(containsString("<td>Liga ACB</td>")))
                .andExpect(content().string(containsString("<td>Combinadas</td>")));
    }
}

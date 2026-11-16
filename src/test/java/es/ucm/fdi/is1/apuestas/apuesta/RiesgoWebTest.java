package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-76: lo que pagaría la casa con cada resultado de un evento y el aviso de riesgo. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RiesgoWebTest {

    private static final RequestPostProcessor CREADOR = user("creador@apuestas.es").roles("CREADOR");
    private static final String ANA = "ana-riesgo@ucm.es";
    private static final String BEA = "bea-riesgo@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RiesgoService riesgo;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private LimitesService limites;

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
        usuarios.crear(BEA, "Bea", "secreta123", Rol.USUARIO);
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        otro = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(),
                equipos.findByNombre("Levante UD").orElseThrow(), Hora.ahora().plusDays(2)));
    }

    private static RiesgoResultado fila(RiesgoEvento r, String simbolo) {
        return r.resultados().stream().filter(f -> f.simbolo().equals(simbolo)).findFirst().orElseThrow();
    }

    @Test
    void cadaResultadoPagaSusApuestasYLasCanceladasNoCuentan() {
        Apuesta local = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));
        Apuesta visitante = apuestas.apostar(BEA, partido.getId(), Resultado.VISITANTE, new BigDecimal("20"));
        Apuesta cancelada = apuestas.apostar(BEA, partido.getId(), Resultado.EMPATE, new BigDecimal("30"));
        apuestas.cancelar(BEA, cancelada.getId());

        RiesgoEvento r = riesgo.delEvento(partido.getId());

        assertThat(r.apostado()).isEqualByComparingTo("30");
        assertThat(r.resultados()).extracting(RiesgoResultado::simbolo).containsExactly("1", "X", "2");
        assertThat(fila(r, "1").pago()).isEqualByComparingTo(local.getGananciaPotencial());
        assertThat(fila(r, "1").saldoCasa()).isEqualByComparingTo(new BigDecimal("30").subtract(local.getGananciaPotencial()));
        assertThat(fila(r, "X").pago()).isZero();
        assertThat(fila(r, "X").saldoCasa()).isEqualByComparingTo("30");
        assertThat(fila(r, "2").pago()).isEqualByComparingTo(visitante.getGananciaPotencial());
    }

    @Test
    void lasDeGolesSeSuponenGanadasYLaDobleOportunidadSegunElResultado() {
        Apuesta goles = apuestas.apostar(ANA, List.of(new SeleccionPedida(partido.getId(), null, Especial.MAS_2_5, null)),
                new BigDecimal("10"));
        Apuesta doble = apuestas.apostar(BEA, List.of(new SeleccionPedida(partido.getId(), null, Especial.DOBLE_1X, null)),
                new BigDecimal("10"));

        RiesgoEvento r = riesgo.delEvento(partido.getId());

        BigDecimal ambas = goles.getGananciaPotencial().add(doble.getGananciaPotencial());
        assertThat(fila(r, "1").pago()).isEqualByComparingTo(ambas);
        assertThat(fila(r, "X").pago()).isEqualByComparingTo(ambas);
        assertThat(fila(r, "2").pago()).isEqualByComparingTo(goles.getGananciaPotencial());
    }

    @Test
    void unaCombinadaCuentaEnteraHastaQueFallaOtraSeleccion() {
        Apuesta combinada = apuestas.apostar(ANA, List.of(new SeleccionPedida(partido.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(otro.getId(), Resultado.LOCAL, null)), new BigDecimal("10"));

        assertThat(fila(riesgo.delEvento(partido.getId()), "1").pago())
                .isEqualByComparingTo(combinada.getGananciaPotencial());

        resolucion.introducirResultado(otro.getId(), Resultado.VISITANTE);
        assertThat(fila(riesgo.delEvento(partido.getId()), "1").pago()).isZero();
    }

    @Test
    void siLaCasaPuedePerderMasQueElLimiteSaleElAvisoYElEventoMarcadoEnElPanel() throws Exception {
        apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("100"));
        assertThat(limites.actuales().getAlertaRiesgo()).isEqualByComparingTo("2000");
        assertThat(riesgo.delEvento(partido.getId()).isAlerta()).isFalse();
        assertThat(riesgo.eventosEnAlerta()).doesNotContain(partido.getId());

        // La pérdida con el 1 es la ganancia menos los 100 apostados: más de 10
        mvc.perform(post("/gestion/limites").with(CREADOR).with(csrf())
                .param("importeMinimo", "1").param("importeMaximo", "500").param("maxSelecciones", "10")
                .param("alertaRiesgo", "10"));
        assertThat(limites.actuales().getAlertaRiesgo()).isEqualByComparingTo("10");

        assertThat(riesgo.delEvento(partido.getId()).isAlerta()).isTrue();
        assertThat(riesgo.eventosEnAlerta()).contains(partido.getId()).doesNotContain(otro.getId());
        mvc.perform(get("/gestion/eventos/{id}", partido.getId()).with(CREADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Riesgo de la casa")))
                .andExpect(content().string(containsString("Riesgo alto")));
        mvc.perform(get("/gestion").with(CREADOR))
                .andExpect(content().string(containsString(">RIESGO</span>")));
        mvc.perform(get("/gestion/eventos/{id}", otro.getId()).with(CREADOR))
                .andExpect(content().string(not(containsString("Riesgo alto"))))
                .andExpect(content().string(containsString("Nadie ha apostado todavía")));
    }

    @Test
    void elLimiteDeAlertaNoPuedeSerNegativo() throws Exception {
        mvc.perform(post("/gestion/limites").with(CREADOR).with(csrf())
                        .param("importeMinimo", "1").param("importeMaximo", "500").param("maxSelecciones", "10")
                        .param("alertaRiesgo", "-1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No puede ser negativo")));
        mvc.perform(get("/gestion/limites").with(CREADOR))
                .andExpect(content().string(containsString("Aviso de riesgo")));
    }
}

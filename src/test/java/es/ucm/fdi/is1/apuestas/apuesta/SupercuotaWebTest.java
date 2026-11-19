package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
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
import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-87: supercuota del creador en un resultado, con importe máximo y fecha de fin. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SupercuotaWebTest {

    private static final RequestPostProcessor CREADOR = user("creador@apuestas.es").roles("CREADOR");
    private static final String ANA = "ana-super@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SupercuotaService supercuotas;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private CalculadoraCuotas calculadora;

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
    private BigDecimal normal;

    @BeforeEach
    void preparar() {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        otro = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(),
                equipos.findByNombre("Levante UD").orElseThrow(), Hora.ahora().plusHours(2)));
        normal = calculadora.calcular(partido).local();
    }

    private BigDecimal mejorada() {
        return normal.add(new BigDecimal("0.50"));
    }

    @Test
    void elCreadorLaPublicaYSaleEnLaPortadaYEnElPartido() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/supercuota", partido.getId()).with(CREADOR).with(csrf())
                        .param("resultado", "LOCAL").param("cuota", mejorada().toPlainString())
                        .param("importeMaximo", "50").param("horas", "24"))
                .andExpect(flash().attribute("mensaje", containsString("publicada")));

        mvc.perform(get("/")).andExpect(content().string(containsString("Supercuotas")));
        mvc.perform(get("/eventos/{id}/apostar", partido.getId()).with(user(ANA)))
                .andExpect(content().string(containsString("Supercuota:")))
                .andExpect(content().string(containsString(mejorada().toPlainString().replace('.', ','))));
    }

    @Test
    void enUnaSimpleSeCobraConLaSupercuotaHastaElImporteMaximo() {
        supercuotas.crear(partido.getId(), Resultado.LOCAL, mejorada(), new BigDecimal("50"), 24);

        Apuesta simple = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("20"));
        assertThat(simple.getCuota()).isEqualByComparingTo(mejorada());

        assertThatThrownBy(() -> apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("60")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("como mucho 50");
        // En una combinada vale la cuota normal
        Apuesta combinada = apuestas.apostar(ANA, List.of(new SeleccionPedida(partido.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(otro.getId(), Resultado.LOCAL, null)), new BigDecimal("60"));
        assertThat(combinada.getSelecciones().get(0).getCuota()).isLessThan(mejorada());
    }

    @Test
    void tieneQueMejorarLaNormalSinPasarDelDoble() {
        assertThatThrownBy(() -> supercuotas.crear(partido.getId(), Resultado.LOCAL, normal, BigDecimal.TEN, 24))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("mejorar la normal");
        assertThatThrownBy(() -> supercuotas.crear(partido.getId(), Resultado.LOCAL,
                normal.multiply(new BigDecimal("2")).add(BigDecimal.ONE), BigDecimal.TEN, 24))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("sin pasar del doble");
    }

    @Test
    void acabaAlEmpezarElPartidoOCuandoLaRetira() {
        Supercuota s = supercuotas.crear(otro.getId(), Resultado.LOCAL,
                calculadora.calcular(otro).local().add(new BigDecimal("0.10")), BigDecimal.TEN, 24);
        assertThat(s.getHasta()).isEqualTo(otro.getFechaHora());

        supercuotas.quitar(otro.getId());
        assertThat(supercuotas.vigente(otro)).isEmpty();
        BigDecimal normalOtro = calculadora.calcular(otro).local();
        assertThat(apuestas.apostar(ANA, otro.getId(), Resultado.LOCAL, BigDecimal.TEN).getCuota())
                .isEqualByComparingTo(normalOtro).isLessThan(s.getCuota());
    }

    @Test
    void elImporteMaximoYLaDuracionTienenLimites() {
        assertThatThrownBy(() -> supercuotas.crear(partido.getId(), Resultado.LOCAL, mejorada(), new BigDecimal("0.10"), 24))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("El importe máximo tiene que estar entre");
        assertThatThrownBy(() -> supercuotas.crear(partido.getId(), Resultado.LOCAL, mejorada(), null, 24))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("El importe máximo");
        for (Integer horas : new Integer[] {null, 0, SupercuotaService.HORAS_MAXIMAS + 1}) {
            assertThatThrownBy(() -> supercuotas.crear(partido.getId(), Resultado.LOCAL, mejorada(), BigDecimal.TEN, horas))
                    .as("horas = %s", horas).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("dura de 1 a");
        }
    }

    @Test
    void soloEnUnEventoQueExisteYAdmiteApuestas() {
        assertThatThrownBy(() -> supercuotas.crear(999_999L, Resultado.LOCAL, mejorada(), BigDecimal.TEN, 24))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("No existe ese evento");
        assertThatThrownBy(() -> supercuotas.crear(partido.getId(), null, mejorada(), BigDecimal.TEN, 24))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Elige un resultado");
        partido.suspender();
        assertThatThrownBy(() -> supercuotas.crear(partido.getId(), Resultado.LOCAL, mejorada(), BigDecimal.TEN, 24))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("ya no admite apuestas");
    }

    @Test
    void noDuraMasQueHastaQueEmpiezaElPartido() {
        BigDecimal mejor = calculadora.calcular(otro).local().add(new BigDecimal("0.10"));
        Supercuota corta = supercuotas.crear(otro.getId(), Resultado.LOCAL, mejor, BigDecimal.TEN, 24);
        assertThat(corta.getHasta()).isEqualTo(otro.getFechaHora());
    }

    @Test
    void retirarLaSupercuotaDeUnEventoQueNoExisteDa404() throws Exception {
        mvc.perform(post("/gestion/eventos/{id}/supercuota/quitar", 999_999).with(CREADOR).with(csrf()))
                .andExpect(status().isNotFound());
    }
}

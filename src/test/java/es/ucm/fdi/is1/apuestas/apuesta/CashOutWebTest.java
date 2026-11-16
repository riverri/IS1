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

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-75: cobrar una apuesta antes de tiempo (cash-out). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CashOutWebTest {

    private static final String ANA = "ana-cashout@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CashOutService cashOut;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private MovimientoSaldoRepository movimientos;

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
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        otro = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(),
                equipos.findByNombre("Levante UD").orElseThrow(), Hora.ahora().plusDays(2)));
    }

    private BigDecimal saldo() {
        return usuarios.findByEmail(ANA).orElseThrow().getSaldo();
    }

    private void muchoDineroAl(Resultado resultado) {
        for (int i = 0; i < 4; i++) {
            String email = "otro" + i + "-cashout@ucm.es";
            usuarioService.crear(email, "Otro " + i, "secreta123", Rol.USUARIO);
            apuestas.apostar(email, partido.getId(), resultado, new BigDecimal("100"));
        }
    }

    @Test
    void recienHechaValeLoApostadoYSuPropioDineroNoLaMueve() throws Exception {
        Apuesta apuesta = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("100"));

        assertThat(cashOut.ofertas(ANA)).containsEntry(apuesta.getId(), new BigDecimal("100.00"));
        mvc.perform(get("/apuestas").with(user(ANA)))
                .andExpect(content().string(containsString("Cobrar ya 100,00")));
    }

    @Test
    void siLaCuotaBajaValeMasSinPasarDeLoQueCobrariaAcertando() {
        Apuesta apuesta = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        muchoDineroAl(Resultado.LOCAL);

        BigDecimal valor = cashOut.ofertas(ANA).get(apuesta.getId());
        assertThat(valor).isGreaterThan(BigDecimal.TEN).isLessThanOrEqualTo(apuesta.getGananciaPotencial());
    }

    @Test
    void enUnaCombinadaCuentaLaSeleccionYaAcertada() {
        Apuesta combinada = apuestas.apostar(ANA, List.of(new SeleccionPedida(otro.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(partido.getId(), Resultado.LOCAL, null)), BigDecimal.TEN);
        BigDecimal cuotaOtro = combinada.getSelecciones().get(0).getCuota();
        otro.cambiarFecha(Hora.ahora().minusHours(2));
        resolucion.introducirResultado(otro.getId(), Resultado.LOCAL);

        assertThat(cashOut.ofertas(ANA).get(combinada.getId()))
                .isEqualByComparingTo(BigDecimal.TEN.multiply(cuotaOtro).setScale(2, java.math.RoundingMode.DOWN));
    }

    @Test
    void siUnPartidoPendienteHaEmpezadoOEstaSuspendidoNoSeOfrece() {
        Apuesta apuesta = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        resolucion.suspender(partido.getId());
        assertThat(cashOut.ofertas(ANA)).doesNotContainKey(apuesta.getId());

        resolucion.reactivar(partido.getId());
        partido.cambiarFecha(Hora.ahora().minusMinutes(5));
        assertThat(cashOut.ofertas(ANA)).doesNotContainKey(apuesta.getId());
        assertThatThrownBy(() -> cashOut.cobrar(ANA, apuesta.getId(), BigDecimal.TEN))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unaVezCobradaElResultadoYaNoLaCambia() throws Exception {
        Apuesta apuesta = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        BigDecimal antes = saldo();

        mvc.perform(post("/apuestas/{id}/cobrar", apuesta.getId()).with(user(ANA)).with(csrf()).param("valorVisto", "10.00"))
                .andExpect(flash().attribute("mensaje", containsString("Apuesta cobrada: 10,00")));
        assertThat(apuesta.getEstado()).isEqualTo(EstadoApuesta.COBRADA);
        assertThat(saldo()).isEqualByComparingTo(antes.add(BigDecimal.TEN));
        assertThat(movimientos.findByUsuarioOrderByIdAsc(usuarios.findByEmail(ANA).orElseThrow()))
                .anyMatch(m -> m.getTipo() == TipoMovimiento.COBRO_ANTICIPADO);

        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);

        assertThat(apuesta.getEstado()).isEqualTo(EstadoApuesta.COBRADA);
        assertThat(saldo()).isEqualByComparingTo(antes.add(BigDecimal.TEN));
        // Cuenta en el balance, pero no como acierto
        Estadisticas estadisticas = apuestas.estadisticas(ANA);
        assertThat(estadisticas.resueltas()).isEqualTo(1);
        assertThat(estadisticas.ganadas()).isZero();
        assertThat(estadisticas.beneficio()).isZero();
    }

    @Test
    void siLasCuotasCambianMientrasDecideNoSeCobraYSeEnsenaLaOfertaNueva() throws Exception {
        Apuesta apuesta = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        BigDecimal antes = saldo();
        muchoDineroAl(Resultado.VISITANTE);
        BigDecimal nueva = cashOut.ofertas(ANA).get(apuesta.getId());
        assertThat(nueva).isLessThan(BigDecimal.TEN);

        mvc.perform(post("/apuestas/{id}/cobrar", apuesta.getId()).with(user(ANA)).with(csrf()).param("valorVisto", "10.00"))
                .andExpect(flash().attribute("aviso", containsString("ahora puedes cobrar "
                        + nueva.toPlainString().replace('.', ','))));
        assertThat(apuesta.getEstado()).isEqualTo(EstadoApuesta.ACTIVA);
        assertThat(saldo()).isEqualByComparingTo(antes);
    }
}

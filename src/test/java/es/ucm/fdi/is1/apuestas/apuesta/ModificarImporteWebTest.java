package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-27: cambiar el importe de una apuesta antes de que empiece el evento. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ModificarImporteWebTest {

    private static final String ANA = "ana@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private CalculadoraCuotas calculadora;

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
    private Apuesta apuesta;

    @BeforeEach
    void apostar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        apuesta = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("20"));
    }

    private BigDecimal saldo() {
        return usuarios.findByEmail(ANA).orElseThrow().getSaldo();
    }

    private void cambiar(String importe) throws Exception {
        mvc.perform(post("/apuestas/{id}/importe", apuesta.getId()).with(COMO_ANA).with(csrf())
                        .param("importe", importe))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/apuestas"));
    }

    @Test
    void subirElImporteCobraLaDiferenciaYAplicaLaCuotaActual() throws Exception {
        BigDecimal antes = saldo();
        BigDecimal cuotaActual = calculadora.calcular(partido).local();

        cambiar("50");

        assertThat(apuesta.getImporte()).isEqualByComparingTo("50");
        assertThat(saldo()).isEqualByComparingTo(antes.subtract(new BigDecimal("30")));
        assertThat(apuesta.getCuota()).isEqualByComparingTo(cuotaActual);
    }

    @Test
    void bajarElImporteDevuelveLaDiferencia() throws Exception {
        BigDecimal antes = saldo();

        cambiar("5");

        assertThat(apuesta.getImporte()).isEqualByComparingTo("5");
        assertThat(saldo()).isEqualByComparingTo(antes.add(new BigDecimal("15")));
    }

    @Test
    void respetaLosLimitesYElSaldo() throws Exception {
        mvc.perform(post("/apuestas/{id}/importe", apuesta.getId()).with(COMO_ANA).with(csrf()).param("importe", "501"))
                .andExpect(flash().attribute("error", containsString("entre 1 y 500")));
        usuarios.findByEmail(ANA).orElseThrow().cargar(saldo().subtract(BigDecimal.TEN));
        mvc.perform(post("/apuestas/{id}/importe", apuesta.getId()).with(COMO_ANA).with(csrf()).param("importe", "40"))
                .andExpect(flash().attribute("error", containsString("saldo suficiente")));
        assertThat(apuesta.getImporte()).isEqualByComparingTo("20");
    }

    @Test
    void noSePuedeCambiarSiElEventoHaEmpezado() throws Exception {
        partido.suspender();

        mvc.perform(post("/apuestas/{id}/importe", apuesta.getId()).with(COMO_ANA).with(csrf()).param("importe", "30"))
                .andExpect(flash().attribute("error", containsString("ya no se puede modificar")));
        assertThat(apuesta.getImporte()).isEqualByComparingTo("20");
    }

    @Test
    void otroUsuarioNoPuedeCambiarla() throws Exception {
        mvc.perform(post("/apuestas/{id}/importe", apuesta.getId()).with(user("usuario@apuestas.es")).with(csrf())
                        .param("importe", "30"))
                .andExpect(status().isNotFound());
        assertThat(apuesta.getImporte()).isEqualByComparingTo("20");
    }

    @Test
    void misApuestasOfreceCambiarElImporte() throws Exception {
        mvc.perform(get("/apuestas").with(COMO_ANA))
                .andExpect(content().string(containsString("Cambiar importe")));
    }
}

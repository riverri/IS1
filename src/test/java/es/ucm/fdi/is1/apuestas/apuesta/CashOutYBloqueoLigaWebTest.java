package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import es.ucm.fdi.is1.apuestas.ligas.BloqueoLigaRepository;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** Pruebas de aceptación para la oferta de HU-102 y el bloqueo individual de HU-105. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CashOutYBloqueoLigaWebTest {

    private static final String ANA = "ana-cashout-bloqueo@ucm.es";
    private static final String LUIS = "luis-cashout-bloqueo@ucm.es";

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private EventoRepository eventos;
    @Autowired private ApuestaService apuestas;
    @Autowired private LigaService ligas;
    @Autowired private BloqueoLigaRepository bloqueos;

    private Evento futbol;
    private Evento baloncesto;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        LocalDateTime manana = Hora.ahora().plusDays(1);
        futbol = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), manana));
        baloncesto = eventos.save(new Evento(competiciones.findByNombre("Liga ACB").orElseThrow(),
                equipos.findByNombre("Unicaja").orElseThrow(),
                equipos.findByNombre("Baskonia").orElseThrow(), manana));
    }

    @Test
    void muestraOfertaDeCashOutParaLaCombinadaCompleta() throws Exception {
        apuestas.apostar(ANA, List.of(
                new SeleccionPedida(futbol.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(baloncesto.getId(), Resultado.VISITANTE, null)), new BigDecimal("20"));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/apuestas").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cobro anticipado disponible: 20,00 monedas por toda la combinada.")));
    }

    @Test
    void bloqueoCuestaCincuentaYElServidorImpideApostarEnElAlcance() throws Exception {
        Liga liga = ligas.crear(ANA, "Amigos");
        ligas.unirse(LUIS, liga.getCodigo());
        BigDecimal saldoInicial = usuarios.findByEmail(ANA).orElseThrow().getSaldo();

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/ligas/{id}/bloqueos", liga.getId()).with(user(ANA)).with(csrf())
                        .param("objetivo", usuarios.findByEmail(LUIS).orElseThrow().getId().toString())
                        .param("evento", futbol.getId().toString()).param("alcance", "LOCAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensaje", containsString("50 monedas")));

        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo())
                .isEqualByComparingTo(saldoInicial.subtract(new BigDecimal("50.00")));
        assertThat(bloqueos.count()).isEqualTo(1);
        assertThatThrownBy(() -> apuestas.apostar(LUIS,
                List.of(new SeleccionPedida(futbol.getId(), Resultado.LOCAL, null)), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("bloqueada");

        // El bloqueo solo limita esa selección, no el partido completo.
        assertThat(apuestas.apostar(LUIS,
                List.of(new SeleccionPedida(futbol.getId(), Resultado.VISITANTE, null)), BigDecimal.TEN)).isNotNull();
    }
}

package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.web.Formato;

/** HU-20, HU-23 y HU-24: cuotas en el catálogo, apuesta simple y apuestas activas. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApuestaWebTest {

    private static final String USUARIO = "usuario@apuestas.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private ApuestaRepository apuestas;

    @Autowired
    private CalculadoraCuotas calculadora;

    @Autowired
    private Formato formato;

    private Evento futbol;
    private Evento baloncesto;
    private Evento yaEmpezado;

    @BeforeEach
    void crearEventos() {
        LocalDateTime ahora = LocalDateTime.now();
        futbol = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), ahora.plusDays(1)));
        baloncesto = eventos.save(new Evento(competiciones.findByNombre("Liga ACB").orElseThrow(),
                equipos.findByNombre("Unicaja").orElseThrow(),
                equipos.findByNombre("Barça Basket").orElseThrow(), ahora.plusDays(1)));
        yaEmpezado = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(),
                equipos.findByNombre("Valencia CF").orElseThrow(), ahora.minusMinutes(5)));
    }

    private BigDecimal saldo() {
        return usuarios.findByEmail(USUARIO).orElseThrow().getSaldo();
    }

    @Test
    void elCatalogoMuestraLasCuotasCalculadas() throws Exception {
        String cuotaLocal = formato.cuota(calculadora.calcular(futbol).local());
        mvc.perform(get("/eventos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(cuotaLocal)))
                .andExpect(content().string(containsString("/boleto/anadir?evento=" + futbol.getId() + "&amp;resultado=EMPATE")));
    }

    @Test
    @WithMockUser(username = USUARIO)
    void apostarDescuentaElSaldoYQuedaActiva() throws Exception {
        BigDecimal antes = saldo();
        BigDecimal cuota = calculadora.calcular(futbol).visitante();

        mvc.perform(post("/eventos/{id}/apostar", futbol.getId()).with(csrf())
                        .param("resultado", "VISITANTE")
                        .param("importe", "25"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/apuestas"));

        assertThat(saldo()).isEqualByComparingTo(antes.subtract(new BigDecimal("25")));
        Apuesta apuesta = apuestas.findAll().stream()
                .filter(a -> a.getSelecciones().get(0).getEvento().getId().equals(futbol.getId()))
                .findFirst().orElseThrow();
        assertThat(apuesta.getEstado()).isEqualTo(EstadoApuesta.ACTIVA);
        assertThat(apuesta.isCombinada()).isFalse();
        assertThat(apuesta.getSelecciones().get(0).getPronostico()).isEqualTo(Resultado.VISITANTE);
        assertThat(apuesta.getCuota()).isEqualByComparingTo(cuota);
    }

    @Test
    @WithMockUser(username = USUARIO)
    void sinSaldoSuficienteNoSePuedeApostar() throws Exception {
        // Se deja el saldo en 20 monedas, por debajo del importe máximo por apuesta
        usuarios.findByEmail(USUARIO).orElseThrow().cargar(saldo().subtract(new BigDecimal("20")));
        BigDecimal antes = saldo();

        mvc.perform(post("/eventos/{id}/apostar", futbol.getId()).with(csrf())
                        .param("resultado", "LOCAL")
                        .param("importe", antes.add(BigDecimal.ONE).toPlainString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No tienes saldo suficiente")));

        assertThat(saldo()).isEqualByComparingTo(antes);
        assertThat(apuestas.count()).isZero();
    }

    @Test
    @WithMockUser(username = USUARIO)
    void rechazaImportesNoValidos() throws Exception {
        mvc.perform(post("/eventos/{id}/apostar", futbol.getId()).with(csrf())
                        .param("resultado", "LOCAL")
                        .param("importe", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El importe tiene que ser mayor que 0")));

        assertThat(apuestas.count()).isZero();
    }

    @Test
    @WithMockUser(username = USUARIO)
    void noSePuedeApostarAlEmpateEnBaloncesto() throws Exception {
        mvc.perform(post("/eventos/{id}/apostar", baloncesto.getId()).with(csrf())
                        .param("resultado", "EMPATE")
                        .param("importe", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no es posible en este evento")));

        assertThat(apuestas.count()).isZero();
    }

    @Test
    @WithMockUser(username = USUARIO)
    void noSePuedeApostarAUnEventoQueYaHaEmpezado() throws Exception {
        mvc.perform(post("/eventos/{id}/apostar", yaEmpezado.getId()).with(csrf())
                        .param("resultado", "LOCAL")
                        .param("importe", "10"))
                .andExpect(status().isNotFound());

        assertThat(apuestas.count()).isZero();
    }

    @Test
    void unVisitanteNoPuedeApostar() throws Exception {
        mvc.perform(post("/eventos/{id}/apostar", futbol.getId()).with(csrf())
                        .param("resultado", "LOCAL")
                        .param("importe", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = USUARIO)
    void misApuestasMuestraLasActivasYLoComprometido() throws Exception {
        mvc.perform(post("/eventos/{id}/apostar", futbol.getId()).with(csrf())
                .param("resultado", "LOCAL").param("importe", "10"));
        mvc.perform(post("/eventos/{id}/apostar", baloncesto.getId()).with(csrf())
                .param("resultado", "VISITANTE").param("importe", "15.50"));

        mvc.perform(get("/apuestas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Getafe CF – Sevilla FC")))
                .andExpect(content().string(containsString("Unicaja – Barça Basket")))
                .andExpect(content().string(containsString("25,50")));
    }

    @Test
    @WithMockUser(username = USUARIO)
    void elFormularioMarcaElResultadoElegidoEnElCatalogo() throws Exception {
        mvc.perform(get("/eventos/{id}/apostar", futbol.getId()).param("resultado", "EMPATE"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesRegex("(?s).*value=\"EMPATE\"[^>]*checked=\"checked\".*")));
    }
}

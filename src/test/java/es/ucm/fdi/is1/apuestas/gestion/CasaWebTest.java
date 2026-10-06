package es.ucm.fdi.is1.apuestas.gestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import es.ucm.fdi.is1.apuestas.apuesta.Apuesta;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.apuesta.CuentasCasa;
import es.ucm.fdi.is1.apuestas.apuesta.CuentasCasaService;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.apuesta.SeleccionPedida;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-54: cuentas de la casa para el creador de apuestas. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CasaWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private CuentasCasaService cuentas;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento futbol;
    private Evento basket;

    @BeforeEach
    void preparar() {
        usuarioService.crear("ana@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear("luis@ucm.es", "Luis", "secreta123", Rol.USUARIO);
        LocalDateTime manana = Hora.ahora().plusDays(1);
        futbol = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), manana));
        basket = eventos.save(new Evento(competiciones.findByNombre("Liga ACB").orElseThrow(),
                equipos.findByNombre("Unicaja").orElseThrow(),
                equipos.findByNombre("Baskonia").orElseThrow(), manana));
    }

    @Test
    void cuentaLoApostadoLoPagadoYElBeneficio() {
        Apuesta gana = apuestas.apostar("ana@ucm.es", futbol.getId(), Resultado.LOCAL, new BigDecimal("10"));
        apuestas.apostar("luis@ucm.es", futbol.getId(), Resultado.VISITANTE, new BigDecimal("20"));
        apuestas.apostar("luis@ucm.es", List.of(new SeleccionPedida(futbol.getId(), Resultado.EMPATE, null),
                new SeleccionPedida(basket.getId(), Resultado.LOCAL, null)), new BigDecimal("5"));
        apuestas.apostar("ana@ucm.es", basket.getId(), Resultado.LOCAL, new BigDecimal("7"));
        Apuesta cancelada = apuestas.apostar("ana@ucm.es", basket.getId(), Resultado.VISITANTE, new BigDecimal("3"));
        apuestas.cancelar("ana@ucm.es", cancelada.getId());

        resolucion.introducirResultado(futbol.getId(), Resultado.LOCAL);

        CuentasCasa c = cuentas.cuentas();
        BigDecimal pagado = gana.getGananciaPotencial();
        assertThat(c.total().apuestas()).isEqualTo(4);
        assertThat(c.total().apostado()).isEqualByComparingTo("35");
        assertThat(c.total().enJuego()).isEqualByComparingTo("7");
        assertThat(c.total().pagado()).isEqualByComparingTo(pagado);
        assertThat(c.total().getBeneficio()).isEqualByComparingTo(new BigDecimal("35").subtract(pagado));
        assertThat(c.jugadores()).isEqualTo(2);
        assertThat(c.porTipo().get(0).apuestas()).isEqualTo(3);
        assertThat(c.porTipo().get(2).apuestas()).isEqualTo(1);
        assertThat(c.porDeporte()).extracting(CuentasCasa.Fila::nombre).containsExactly("⚽ Fútbol", "🏀 Baloncesto");
        assertThat(c.margenTeorico()).isEqualByComparingTo("6.5");
    }

    @Test
    void elCreadorVeElPanelYUnUsuarioNo() throws Exception {
        mvc.perform(get("/gestion/casa").with(user("creador@apuestas.es").roles("CREADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Margen real")))
                .andExpect(content().string(containsString("6,5 %")))
                .andExpect(content().string(containsString("Simples por deporte")));
        mvc.perform(get("/gestion/casa").with(user("ana@ucm.es"))).andExpect(status().isForbidden());
    }
}

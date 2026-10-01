package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-26: cancelar una apuesta activa antes de que empiece el evento. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CancelacionWebTest {

    private static final String USUARIO = "usuario@apuestas.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApuestaService apuestaService;

    @Autowired
    private ApuestaRepository apuestas;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private UsuarioService usuarioService;

    private Evento evento(LocalDateTime fecha) {
        return eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(), fecha));
    }

    private BigDecimal saldo(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo();
    }

    @Test
    @WithMockUser(username = USUARIO)
    void cancelarDevuelveElImporte() throws Exception {
        BigDecimal antes = saldo(USUARIO);
        Apuesta apuesta = apuestaService.apostar(USUARIO, evento(LocalDateTime.now().plusDays(1)).getId(),
                Resultado.LOCAL, new BigDecimal("40"));

        mvc.perform(post("/apuestas/{id}/cancelar", apuesta.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/apuestas"))
                .andExpect(flash().attributeExists("mensaje"));

        assertThat(apuestas.findById(apuesta.getId()).orElseThrow().getEstado()).isEqualTo(EstadoApuesta.CANCELADA);
        assertThat(saldo(USUARIO)).isEqualByComparingTo(antes);
    }

    @Test
    @WithMockUser(username = USUARIO)
    void noSePuedeCancelarSiElEventoYaHaEmpezado() throws Exception {
        Usuario usuario = usuarios.findByEmail(USUARIO).orElseThrow();
        Apuesta apuesta = new Apuesta(usuario, new BigDecimal("10"), LocalDateTime.now().minusDays(1));
        apuesta.anadir(evento(LocalDateTime.now().minusMinutes(10)), Resultado.LOCAL, new BigDecimal("2.00"));
        apuestas.save(apuesta);
        BigDecimal antes = saldo(USUARIO);

        mvc.perform(post("/apuestas/{id}/cancelar", apuesta.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertThat(apuestas.findById(apuesta.getId()).orElseThrow().getEstado()).isEqualTo(EstadoApuesta.ACTIVA);
        assertThat(saldo(USUARIO)).isEqualByComparingTo(antes);
    }

    @Test
    @WithMockUser(username = USUARIO)
    void noSePuedeCancelarLaApuestaDeOtro() throws Exception {
        usuarioService.crear("otro@ucm.es", "Otro", "secreta123", Rol.USUARIO);
        Apuesta ajena = apuestaService.apostar("otro@ucm.es", evento(LocalDateTime.now().plusDays(1)).getId(),
                Resultado.VISITANTE, new BigDecimal("20"));

        mvc.perform(post("/apuestas/{id}/cancelar", ajena.getId()).with(csrf()))
                .andExpect(status().isNotFound());

        assertThat(apuestas.findById(ajena.getId()).orElseThrow().getEstado()).isEqualTo(EstadoApuesta.ACTIVA);
    }
}

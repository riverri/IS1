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
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.mercados.Candidato;
import es.ucm.fdi.is1.apuestas.mercados.EstadoMercado;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;
import es.ucm.fdi.is1.apuestas.mercados.MercadoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-44 y HU-45: apuestas a largo plazo y su gestión por el creador. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LargoPlazoWebTest {

    private static final RequestPostProcessor CREADOR = user("creador@apuestas.es").roles("CREADOR");
    private static final String ANA = "ana@ucm.es";
    private static final String LUIS = "luis@ucm.es";
    private static final RequestPostProcessor COMO_ANA = user(ANA).roles("USUARIO");
    private static final RequestPostProcessor COMO_LUIS = user(LUIS).roles("USUARIO");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MercadoRepository mercados;

    @Autowired
    private ApuestaRepository apuestas;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private UsuarioService usuarioService;

    private Mercado pichichi;
    private Candidato mbappe;
    private Candidato ferran;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        pichichi = new Mercado("Pichichi de prueba", Deporte.FUTBOL, Hora.ahora().plusDays(30));
        mbappe = pichichi.anadirCandidato("Kylian Mbappé", new BigDecimal("2.20"));
        ferran = pichichi.anadirCandidato("Ferran Torres", new BigDecimal("8.00"));
        pichichi = mercados.saveAndFlush(pichichi);
        mbappe = pichichi.getCandidatos().get(0);
        ferran = pichichi.getCandidatos().get(1);
    }

    private BigDecimal saldo(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo();
    }

    private Apuesta apostar(RequestPostProcessor quien, Candidato candidato, String importe) throws Exception {
        long antes = apuestas.count();
        mvc.perform(post("/mercados/{id}/apostar", pichichi.getId()).with(quien).with(csrf())
                        .param("candidato", candidato.getId().toString())
                        .param("cuota", candidato.getCuota().toPlainString())
                        .param("importe", importe))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/apuestas"));
        assertThat(apuestas.count()).isEqualTo(antes + 1);
        return apuestas.findAll().stream().filter(Apuesta::isLargoPlazo)
                .filter(a -> a.getSelecciones().get(0).getCandidato().getId().equals(candidato.getId()))
                .filter(a -> a.getUsuario().getEmail().equals(quien == COMO_ANA ? ANA : LUIS))
                .findFirst().orElseThrow();
    }

    @Test
    void laPaginaEsPublicaYMuestraLosMercadosAbiertos() throws Exception {
        mvc.perform(get("/mercados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pichichi de prueba")))
                .andExpect(content().string(containsString("Kylian Mbappé")))
                .andExpect(content().string(containsString("2,20")))
                .andExpect(content().string(containsString("Balón de Oro 2027")))
                .andExpect(content().string(containsString("Entra para apostar")));
    }

    @Test
    void paraApostarHayQueIniciarSesion() throws Exception {
        mvc.perform(post("/mercados/{id}/apostar", pichichi.getId()).with(csrf())
                        .param("candidato", mbappe.getId().toString()).param("importe", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        assertThat(apuestas.count()).isZero();
    }

    @Test
    void apostarDescuentaElSaldoYApareceEnMisApuestas() throws Exception {
        BigDecimal antes = saldo(ANA);

        Apuesta apuesta = apostar(COMO_ANA, ferran, "20");

        assertThat(saldo(ANA)).isEqualByComparingTo(antes.subtract(new BigDecimal("20")));
        assertThat(apuesta.getEstado()).isEqualTo(EstadoApuesta.ACTIVA);
        assertThat(apuesta.getCuota()).isEqualByComparingTo("8.00");
        assertThat(apuesta.getGananciaPotencial()).isEqualByComparingTo("160.00");
        mvc.perform(get("/apuestas").with(COMO_ANA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Largo plazo")))
                .andExpect(content().string(containsString("Ferran Torres")))
                .andExpect(content().string(containsString("Pichichi de prueba")));
    }

    @Test
    void siElCreadorCambiaLaCuotaNoSeApuestaConLaVieja() throws Exception {
        mvc.perform(post("/gestion/mercados/{id}/candidatos/{c}/cuota", pichichi.getId(), mbappe.getId())
                        .with(CREADOR).with(csrf()).param("cuota", "1.90"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(post("/mercados/{id}/apostar", pichichi.getId()).with(COMO_ANA).with(csrf())
                        .param("candidato", mbappe.getId().toString())
                        .param("cuota", "2.20")
                        .param("importe", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("La cuota ha cambiado")));
        assertThat(apuestas.count()).isZero();
    }

    @Test
    void unMercadoCerradoNoAdmiteApuestas() throws Exception {
        mvc.perform(post("/gestion/mercados/{id}/cerrar", pichichi.getId()).with(CREADOR).with(csrf()))
                .andExpect(status().is3xxRedirection());

        mvc.perform(post("/mercados/{id}/apostar", pichichi.getId()).with(COMO_ANA).with(csrf())
                        .param("candidato", mbappe.getId().toString()).param("importe", "10"))
                .andExpect(flash().attribute("error", containsString("ya no admite apuestas")));
        assertThat(apuestas.count()).isZero();
        mvc.perform(get("/mercados"))
                .andExpect(content().string(containsString("Cerrados y resueltos")));
    }

    @Test
    void alMarcarElGanadorSePaganLasApuestasYSePuedeCorregir() throws Exception {
        Apuesta deAna = apostar(COMO_ANA, mbappe, "10");
        Apuesta deLuis = apostar(COMO_LUIS, ferran, "10");
        BigDecimal anaAntes = saldo(ANA);
        BigDecimal luisAntes = saldo(LUIS);

        mvc.perform(post("/gestion/mercados/{id}/ganador", pichichi.getId()).with(CREADOR).with(csrf())
                        .param("candidato", mbappe.getId().toString()))
                .andExpect(flash().attribute("mensaje", containsString("Apuestas resueltas: 2")));

        assertThat(pichichi.getEstado()).isEqualTo(EstadoMercado.RESUELTO);
        assertThat(deAna.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        assertThat(deLuis.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes.add(new BigDecimal("22.00")));
        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes);

        // Corrección: el ganador era Ferran
        mvc.perform(post("/gestion/mercados/{id}/ganador", pichichi.getId()).with(CREADOR).with(csrf())
                        .param("candidato", ferran.getId().toString()))
                .andExpect(status().is3xxRedirection());

        assertThat(deAna.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
        assertThat(deLuis.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        assertThat(saldo(ANA)).isEqualByComparingTo(anaAntes);
        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes.add(new BigDecimal("80.00")));
    }

    @Test
    void anularElMercadoDevuelveElImporte() throws Exception {
        BigDecimal antes = saldo(ANA);
        Apuesta deAna = apostar(COMO_ANA, mbappe, "15");

        mvc.perform(post("/gestion/mercados/{id}/anular", pichichi.getId()).with(CREADOR).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(deAna.getEstado()).isEqualTo(EstadoApuesta.ANULADA);
        assertThat(saldo(ANA)).isEqualByComparingTo(antes);
    }

    @Test
    void sePuedeCancelarMientrasElMercadoEsteAbierto() throws Exception {
        BigDecimal antes = saldo(ANA);
        Apuesta deAna = apostar(COMO_ANA, mbappe, "15");

        mvc.perform(post("/apuestas/{id}/cancelar", deAna.getId()).with(COMO_ANA).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(deAna.getEstado()).isEqualTo(EstadoApuesta.CANCELADA);
        assertThat(saldo(ANA)).isEqualByComparingTo(antes);
    }

    @Test
    void elCreadorDaDeAltaUnMercadoConSusCandidatos() throws Exception {
        mvc.perform(post("/gestion/mercados/nuevo").with(CREADOR).with(csrf())
                        .param("nombre", "MVP de la ACB 2026/27")
                        .param("deporte", "BALONCESTO")
                        .param("cierre", Hora.ahora().plusMonths(3).withNano(0).withSecond(0).toString())
                        .param("candidatos", "Mario Hezonja; 4,50\nEdy Tavares; 6\n\nUnicaja; 12"))
                .andExpect(status().is3xxRedirection());

        Mercado creado = mercados.findByNombre("MVP de la ACB 2026/27").orElseThrow();
        assertThat(creado.getCandidatos()).extracting(Candidato::getNombre)
                .containsExactly("Mario Hezonja", "Edy Tavares", "Unicaja");
        assertThat(creado.getCandidatos().get(0).getCuota()).isEqualByComparingTo("4.50");
        // "Unicaja" es un equipo de baloncesto del catálogo: se enlaza para mostrar su escudo
        assertThat(creado.getCandidatos().get(2).getEquipo()).isNotNull();
    }

    @Test
    void altaDeMercadoRechazaLineasMalEscritas() throws Exception {
        mvc.perform(post("/gestion/mercados/nuevo").with(CREADOR).with(csrf())
                        .param("nombre", "Mercado roto")
                        .param("deporte", "FUTBOL")
                        .param("cierre", Hora.ahora().plusMonths(3).withNano(0).withSecond(0).toString())
                        .param("candidatos", "Mbappé 2,5\nKane; 4"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El formato es: Nombre; cuota")));
        assertThat(mercados.findByNombre("Mercado roto")).isEmpty();
    }

    @Test
    void unUsuarioNormalNoGestionaMercados() throws Exception {
        mvc.perform(post("/gestion/mercados/{id}/ganador", pichichi.getId()).with(COMO_ANA).with(csrf())
                        .param("candidato", mbappe.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void elCreadorVeElDineroApostadoACadaCandidato() throws Exception {
        apostar(COMO_ANA, ferran, "35");

        mvc.perform(get("/gestion/mercados/{id}", pichichi.getId()).with(CREADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ferran Torres")))
                .andExpect(content().string(containsString("35,00")));
    }
}

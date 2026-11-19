package es.ucm.fdi.is1.apuestas.seguimiento;

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
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-86 (equipos favoritos) y HU-90 (alertas de cuota). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguimientoWebTest {

    private static final String ANA = "ana-sigue@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FavoritosService favoritos;

    @Autowired
    private AlertasService alertas;

    @Autowired
    private AlertaCuotaRepository alertaRepository;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private CalculadoraCuotas calculadora;

    @Autowired
    private NotificacionService notificaciones;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Equipo getafe;
    private Evento partido;

    @BeforeEach
    void preparar() {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        getafe = equipos.findByNombre("Getafe CF").orElseThrow();
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(), getafe,
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
    }

    // --- HU-86 ---

    @Test
    void marcoUnEquipoComoFavoritoDesdeSuFichaYLoPuedoQuitar() throws Exception {
        mvc.perform(post("/equipos/{id}/favorito", getafe.getId()).with(user(ANA)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(favoritos.esFavorito(ANA, getafe.getId())).isTrue();
        mvc.perform(get("/equipos/{id}", getafe.getId()).with(user(ANA)))
                .andExpect(content().string(containsString("Quitar de favoritos")));

        mvc.perform(post("/equipos/{id}/favorito", getafe.getId()).with(user(ANA)).with(csrf()));
        assertThat(favoritos.esFavorito(ANA, getafe.getId())).isFalse();
    }

    @Test
    void susPartidosSalenLosPrimerosEnElCatalogo() throws Exception {
        favoritos.alternar(ANA, getafe.getId());

        assertThat(favoritos.proximosPartidos(ANA)).contains(partido)
                .allMatch(e -> e.getLocal().equals(getafe) || e.getVisitante().equals(getafe));
        mvc.perform(get("/eventos").with(user(ANA)))
                .andExpect(content().string(containsString("Tus equipos")));
        mvc.perform(get("/eventos").with(user("usuario@apuestas.es")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Tus equipos"))));
    }

    @Test
    void cuandoSePublicaUnPartidoDeMiEquipoMeLlegaUnAviso() throws Exception {
        favoritos.alternar(ANA, getafe.getId());
        long antes = notificaciones.sinLeer(ANA);

        mvc.perform(post("/gestion/eventos/nuevo").with(user("creador@apuestas.es").roles("CREADOR")).with(csrf())
                        .param("competicionId", competiciones.findByNombre("LaLiga").orElseThrow().getId().toString())
                        .param("localId", equipos.findByNombre("Elche CF").orElseThrow().getId().toString())
                        .param("visitanteId", getafe.getId().toString())
                        .param("fechaHora", Hora.ahora().plusDays(3).withSecond(0).withNano(0)
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().is3xxRedirection());

        assertThat(notificaciones.sinLeer(ANA)).isEqualTo(antes + 1);
        assertThat(notificaciones.leer(ANA).get(0).texto()).startsWith("Nuevo partido de tus equipos: Elche CF – Getafe CF");
    }

    @Test
    void elCreadorNoSigueEquipos() {
        assertThatThrownBy(() -> favoritos.alternar("creador@apuestas.es", getafe.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- HU-90 ---

    @Test
    void laCuotaDeLaAlertaTieneQueSerMayorQueLaActual() throws Exception {
        BigDecimal actual = calculadora.calcular(partido).local();

        mvc.perform(post("/eventos/{id}/alertas", partido.getId()).with(user(ANA)).with(csrf())
                        .param("resultado", "LOCAL").param("cuota", actual.toPlainString()))
                .andExpect(flash().attribute("errorAlerta", containsString("tiene que ser mayor que la actual")));
        mvc.perform(post("/eventos/{id}/alertas", partido.getId()).with(user(ANA)).with(csrf())
                        .param("resultado", "LOCAL").param("cuota", actual.add(new BigDecimal("0.50")).toPlainString()))
                .andExpect(flash().attribute("mensajeAlerta", containsString("Alerta guardada")));
        mvc.perform(get("/eventos/{id}/apostar", partido.getId()).with(user(ANA)))
                .andExpect(content().string(containsString("pendiente")));
    }

    @Test
    void cuandoLaCuotaLlegaAlValorMeAvisaUnaSolaVez() {
        BigDecimal actual = calculadora.calcular(partido).local();
        AlertaCuota alerta = alertas.crear(ANA, partido.getId(), Resultado.LOCAL, actual.add(new BigDecimal("0.01")));

        // Mucho dinero al visitante: su cuota baja y la del local sube
        for (int i = 0; i < 4; i++) {
            String email = "apostador" + i + "-alerta@ucm.es";
            usuarios.crear(email, "Apostador " + i, "secreta123", Rol.USUARIO);
            apuestas.apostar(email, partido.getId(), Resultado.VISITANTE, new BigDecimal("100"));
        }

        assertThat(calculadora.calcular(partido).local()).isGreaterThanOrEqualTo(alerta.getCuota());
        assertThat(alerta.isPendiente()).isFalse();
        List<NotificacionService.Aviso> avisos = notificaciones.leer(ANA);
        assertThat(avisos).hasSize(1);
        assertThat(avisos.get(0).texto()).startsWith("La cuota de «Gana el local» en Getafe CF – Sevilla FC ha llegado a");

        alertas.repasar();
        assertThat(notificaciones.sinLeer(ANA)).isZero();
    }

    @Test
    void lasAlertasDeUnPartidoYaEmpezadoSeBorran() {
        BigDecimal actual = calculadora.calcular(partido).local();
        AlertaCuota alerta = alertas.crear(ANA, partido.getId(), Resultado.LOCAL, actual.add(BigDecimal.ONE));
        partido.cambiarFecha(Hora.ahora().minusMinutes(1));

        alertas.repasar();

        assertThat(alertaRepository.findById(alerta.getId())).isEmpty();
    }

    @Test
    void unaAlertaSinResultadoOEnUnPartidoQueNoExisteAvisaSinError() throws Exception {
        mvc.perform(post("/eventos/{id}/alertas", partido.getId()).with(user(ANA)).with(csrf()).param("cuota", "9"))
                .andExpect(flash().attribute("errorAlerta", containsString("Elige el resultado")));
        mvc.perform(post("/eventos/{id}/alertas", 999_999).with(user(ANA)).with(csrf())
                        .param("resultado", "LOCAL").param("cuota", "9"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorAlerta", "No existe ese partido"));
    }

    @Test
    void comoMuchoTresAlertasPendientesPorPartido() {
        BigDecimal actual = calculadora.calcular(partido).local();
        for (int i = 1; i <= AlertasService.MAXIMO_POR_EVENTO; i++) {
            alertas.crear(ANA, partido.getId(), Resultado.LOCAL, actual.add(BigDecimal.valueOf(i)));
        }
        assertThatThrownBy(() -> alertas.crear(ANA, partido.getId(), Resultado.LOCAL, actual.add(BigDecimal.TEN)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Como mucho 3");
    }

    @Test
    void soloBorroMisAlertas() throws Exception {
        usuarios.crear("otro-sigue@ucm.es", "Otro", "secreta123", Rol.USUARIO);
        AlertaCuota mia = alertas.crear(ANA, partido.getId(), Resultado.LOCAL,
                calculadora.calcular(partido).local().add(BigDecimal.ONE));

        mvc.perform(post("/alertas/{id}/borrar", mia.getId()).with(user("otro-sigue@ucm.es")).with(csrf()))
                .andExpect(flash().attribute("error", "No existe esa alerta"));
        assertThat(alertaRepository.findById(mia.getId())).isPresent();

        mvc.perform(post("/alertas/{id}/borrar", mia.getId()).with(user(ANA)).with(csrf()))
                .andExpect(flash().attribute("mensajeAlerta", "Alerta borrada."));
        assertThat(alertaRepository.findById(mia.getId())).isEmpty();
    }
}

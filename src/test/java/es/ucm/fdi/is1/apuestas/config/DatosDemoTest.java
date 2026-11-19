package es.ucm.fdi.is1.apuestas.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.Apuesta;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaRepository;
import es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.ligas.LigaRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Datos de demostración ({@link DatosDemo}): con {@code apuestas.demo.activa=true} la web arranca con jugadores,
 * apuestas resueltas y activas, partidos próximos, una supercuota y una liga; y no se cargan dos veces.
 * Con su propia base de datos en memoria, para no mezclar estos datos con los del resto de pruebas.
 */
@SpringBootTest(properties = {"apuestas.demo.activa=true", "spring.datasource.url=jdbc:h2:mem:apuestas-demo"})
@AutoConfigureMockMvc
@DirtiesContext
class DatosDemoTest {

    private static final List<String> JUGADORES = List.of("lucia.demo@apuestas.es", "marcos.demo@apuestas.es",
            "sofia.demo@apuestas.es", "pablo.demo@apuestas.es", "elena.demo@apuestas.es", "hugo.demo@apuestas.es");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DatosDemo demo;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private ApuestaRepository apuestas;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private LigaRepository ligas;

    @Test
    @Transactional
    void hayJugadoresConApuestasGanadasPerdidasYActivas() {
        JUGADORES.forEach(email -> assertThat(usuarios.findByEmail(email)).as(email).isPresent());
        List<Apuesta> deLaDemo = apuestas.findAll().stream()
                .filter(a -> JUGADORES.contains(a.getUsuario().getEmail())).toList();
        assertThat(deLaDemo).extracting(Apuesta::getEstado)
                .contains(EstadoApuesta.GANADA, EstadoApuesta.PERDIDA, EstadoApuesta.ACTIVA);
        assertThat(deLaDemo).anyMatch(Apuesta::isCombinada);
        // El usuario de prueba también tiene apuestas, para enseñar «Mis apuestas» al entrar con él
        assertThat(apuestas.findAll()).anyMatch(a -> a.getUsuario().getEmail().equals("usuario@apuestas.es"));
    }

    @Test
    void hayPartidosProximosConSupercuotaYUnRankingConVariosJugadores() throws Exception {
        LocalDateTime ahora = LocalDateTime.now();
        assertThat(eventos.findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento.PROGRAMADO, ahora))
                .hasSizeGreaterThanOrEqualTo(6);
        assertThat(eventos.findAll()).filteredOn(e -> e.getEstado() == EstadoEvento.FINALIZADO).hasSizeGreaterThanOrEqualTo(4);
        mvc.perform(get("/")).andExpect(content().string(containsString("Supercuotas")));
        mvc.perform(get("/ranking"))
                .andExpect(content().string(containsString("Lucía")))
                .andExpect(content().string(containsString("Hugo")));
    }

    @Test
    @Transactional
    void hayUnaLigaConVariosJugadores() {
        assertThat(ligas.findAll()).anySatisfy(l -> {
            assertThat(l.getNombre()).isEqualTo("Liga de la clase");
            assertThat(l.getMiembrosActivos()).hasSizeGreaterThanOrEqualTo(5);
        });
    }

    @Test
    void noSeCarganDosVeces() {
        long usuariosAntes = usuarios.count();
        long apuestasAntes = apuestas.count();
        demo.cargar();
        assertThat(usuarios.count()).isEqualTo(usuariosAntes);
        assertThat(apuestas.count()).isEqualTo(apuestasAntes);
    }
}

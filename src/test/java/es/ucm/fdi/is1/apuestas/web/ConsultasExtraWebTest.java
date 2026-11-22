package es.ucm.fdi.is1.apuestas.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.anuncios.AnuncioService;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.JugadorRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.fantasy.FantasyService;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaService;
import es.ucm.fdi.is1.apuestas.mercados.MercadoService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/** Recuentos de consultas de páginas que ConsultasWebTest no mide: con pocas filas y con 10 más. */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Transactional
@DirtiesContext
class ConsultasExtraWebTest {

    private static final String ANA = "ana-extra@ucm.es";

    @Autowired private MockMvc mvc;
    @Autowired private EntityManager em;
    @Autowired private EntityManagerFactory emf;
    @Autowired private UsuarioService usuarios;
    @Autowired private AnuncioService anuncios;
    @Autowired private LigaService ligas;
    @Autowired private FantasyService fantasy;
    @Autowired private JugadorRepository jugadores;
    @Autowired private MercadoService mercados;
    @Autowired private ApuestaService apuestas;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private EventoRepository eventos;

    private int n;

    @BeforeEach
    void preparar() {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
    }

    private long consultas(MockHttpServletRequestBuilder peticion) throws Exception {
        em.flush();
        em.clear();
        anuncios.invalidarCacheAnuncios();
        Statistics estadisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estadisticas.clear();
        mvc.perform(peticion).andExpect(status().isOk());
        return estadisticas.getPrepareStatementCount();
    }

    private String usuario() {
        String email = "extra-" + (n++) + "@ucm.es";
        usuarios.crear(email, "Jugador " + n, "secreta123", Rol.USUARIO);
        return email;
    }

    private void informe(String pagina, long pocas, long muchas) {
        System.out.println("CONSULTAS-EXTRA " + pagina + ": " + pocas + " -> " + muchas);
    }

    @Test
    void ligas() throws Exception {
        Liga liga = ligas.crear(ANA, "Liga base");
        for (int i = 0; i < 2; i++) {
            ligas.unirse(usuario(), liga.getCodigo());
        }
        long pocas = consultas(get("/ligas").with(user(ANA)));
        long pocasCl = consultas(get("/ligas/clasificacion").with(user(ANA)));
        long pocasGl = consultas(get("/ligas/clasificacion?vista=global"));
        long pocasLiga = consultas(get("/ligas/" + liga.getId()).with(user(ANA)));
        for (int i = 0; i < 10; i++) {
            String otro = usuario();
            ligas.unirse(otro, liga.getCodigo());
            Liga suya = ligas.crear(otro, "Liga " + i);
            ligas.cambiarVisibilidad(suya.getId(), otro, true);
        }
        for (int i = 0; i < 4; i++) {
            Liga extra = ligas.crear(usuario(), "Extra " + i);
            ligas.unirse(ANA, extra.getCodigo());
        }
        informe("/ligas", pocas, consultas(get("/ligas").with(user(ANA))));
        informe("/ligas/clasificacion", pocasCl, consultas(get("/ligas/clasificacion").with(user(ANA))));
        informe("/ligas/clasificacion global", pocasGl, consultas(get("/ligas/clasificacion?vista=global")));
        informe("/ligas/{id}", pocasLiga, consultas(get("/ligas/" + liga.getId()).with(user(ANA))));
    }

    @Test
    void fantasy() throws Exception {
        Equipo getafe = equipos.findByNombre("Getafe CF").orElseThrow();
        var portero = new es.ucm.fdi.is1.apuestas.equipos.Jugador(getafe, "Portero extra",
                es.ucm.fdi.is1.apuestas.equipos.Posicion.PORTERO);
        portero.cambiarNota(6);
        jugadores.save(portero);
        fantasy.crear(ANA, "Ana FC");
        fantasy.fichar(ANA, portero.getId());
        for (int i = 0; i < 2; i++) {
            String otro = usuario();
            fantasy.crear(otro, "Equipo " + n);
        }
        long pocas = consultas(get("/fantasy").with(user(ANA)));
        long pocasMercado = consultas(get("/fantasy/mercado").with(user(ANA)));
        for (int i = 0; i < 10; i++) {
            String otro = usuario();
            fantasy.crear(otro, "Equipo " + n);
        }
        informe("/fantasy", pocas, consultas(get("/fantasy").with(user(ANA))));
        informe("/fantasy/mercado", pocasMercado, consultas(get("/fantasy/mercado").with(user(ANA))));
    }

    @Test
    void mercadosYCasa() throws Exception {
        for (int i = 0; i < 2; i++) {
            mercados.crear("Mercado " + i, Deporte.FUTBOL, Hora.ahora().plusDays(3), "A " + i + ";2.5\nB " + i + ";3");
        }
        var liga = competiciones.findByNombre("LaLiga").orElseThrow();
        List<Evento> creados = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Equipo local = equipos.save(new Equipo("Local extra " + i, liga.getDeporte(), 6.0));
            Equipo visitante = equipos.save(new Equipo("Visitante extra " + i, liga.getDeporte(), 5.0));
            creados.add(eventos.save(new Evento(liga, local, visitante, Hora.ahora().plusDays(1).plusMinutes(i))));
        }
        for (int i = 0; i < 2; i++) {
            apuestas.apostar(usuario(), creados.get(i).getId(), Resultado.LOCAL, BigDecimal.ONE);
        }
        var creador = user("creador@apuestas.es").roles("CREADOR");
        long pocas = consultas(get("/mercados").with(user(ANA)));
        long pocasCasa = consultas(get("/gestion/casa").with(creador));
        long pocasNot = consultas(get("/notificaciones").with(user(ANA)));
        for (int i = 2; i < 12; i++) {
            mercados.crear("Mercado " + i, Deporte.FUTBOL, Hora.ahora().plusDays(3), "A " + i + ";2.5\nB " + i + ";3");
            apuestas.apostar(usuario(), creados.get(i).getId(), Resultado.LOCAL, BigDecimal.ONE);
        }
        informe("/mercados", pocas, consultas(get("/mercados").with(user(ANA))));
        informe("/gestion/casa", pocasCasa, consultas(get("/gestion/casa").with(creador)));
        informe("/notificaciones", pocasNot, consultas(get("/notificaciones").with(user(ANA))));
    }
}

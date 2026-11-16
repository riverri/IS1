package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cripto.CriptoService;
import es.ucm.fdi.is1.apuestas.cripto.FuentePrecios;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.prediccion.Lado;
import es.ucm.fdi.is1.apuestas.prediccion.MercadoPrediccion;
import es.ucm.fdi.is1.apuestas.prediccion.PrediccionService;
import es.ucm.fdi.is1.apuestas.quiniela.Quiniela;
import es.ucm.fdi.is1.apuestas.quiniela.QuinielaService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-84: ranking filtrado por tipo de juego. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RankingPorJuegoWebTest {

    private static final String ANA = "ana-juegos@ucm.es";
    private static final String LUIS = "luis-juegos@ucm.es";
    private static final String EVA = "eva-juegos@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RankingService ranking;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private PrediccionService predicciones;

    @Autowired
    private CriptoService cripto;

    @Autowired
    private QuinielaService quinielas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @MockitoBean
    private FuentePrecios fuente;

    @BeforeEach
    void preparar() {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarios.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        usuarios.crear(EVA, "Eva", "secreta123", Rol.USUARIO);
        given(fuente.precios(any())).willReturn(Map.of(
                "dogecoin", new FuentePrecios.Precio(new BigDecimal("0.20"), null),
                "bitcoin", new FuentePrecios.Precio(new BigDecimal("84312.55"), null)));
        cripto.actualizarPrecios();
    }

    private List<String> nombres(TipoJuego juego, CriterioRanking criterio) {
        return ranking.ranking(null, criterio, null, juego).stream().map(PuestoRanking::nombre).toList();
    }

    private PuestoRanking de(String nombre, TipoJuego juego) {
        return ranking.ranking(null, CriterioRanking.GANANCIAS, null, juego).stream()
                .filter(p -> p.nombre().equals(nombre)).findFirst().orElseThrow();
    }

    @Test
    void enPrediccionesSoloSalenLosQueHanJugadoConSusCifras() throws Exception {
        MercadoPrediccion mercado = predicciones.crear("¿Lloverá en la final?", null, Hora.ahora().plusDays(5),
                new BigDecimal("100"), new BigDecimal("2"));
        predicciones.comprar(ANA, mercado.getId(), Lado.SI, new BigDecimal("20"), null);
        predicciones.comprar(LUIS, mercado.getId(), Lado.NO, new BigDecimal("20"), null);
        predicciones.resolver(mercado.getId(), Lado.SI);

        assertThat(nombres(TipoJuego.PREDICCIONES, CriterioRanking.GANANCIAS)).containsExactly("Ana", "Luis");
        PuestoRanking ana = de("Ana", TipoJuego.PREDICCIONES);
        assertThat(ana.estadisticas().resueltas()).isEqualTo(1);
        assertThat(ana.estadisticas().getPorcentajeAciertos()).isEqualByComparingTo("100");
        assertThat(ana.estadisticas().beneficio()).isPositive();
        assertThat(de("Luis", TipoJuego.PREDICCIONES).estadisticas().beneficio()).isEqualByComparingTo("-20");

        mvc.perform(get("/ranking").param("juego", "PREDICCIONES"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana")))
                .andExpect(content().string(not(containsString("Eva"))))
                .andExpect(content().string(containsString("% de aciertos")));
    }

    @Test
    void cadaCategoriaDeCriptoVaPorSeparadoYSinAciertos() throws Exception {
        cripto.comprar(ANA, "DOGE", new BigDecimal("100"), null);
        cripto.comprar(LUIS, "BTC", new BigDecimal("50"), null);

        assertThat(nombres(TipoJuego.MEMECOINS, CriterioRanking.GANANCIAS)).containsExactly("Ana");
        assertThat(nombres(TipoJuego.CRIPTO, CriterioRanking.GANANCIAS)).containsExactly("Luis");
        // Lo que vale hoy menos lo pagado: la comisión hace que se pierda algo al comprar
        PuestoRanking ana = de("Ana", TipoJuego.MEMECOINS);
        assertThat(ana.estadisticas().apostado()).isEqualByComparingTo("100");
        assertThat(ana.estadisticas().beneficio()).isNegative().isGreaterThan(new BigDecimal("-100"));

        mvc.perform(get("/ranking").param("juego", "MEMECOINS").param("orden", "ACIERTOS"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("% de aciertos"))))
                .andExpect(content().string(containsString("Aquí no se acierta ni se falla")));
    }

    @Test
    void conUnTipoDeJuegoNoSeOrdenaPorSaldo() throws Exception {
        mvc.perform(get("/ranking").param("juego", "DEPORTES").param("orden", "SALDO"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(">Saldo</a>"))))
                .andExpect(content().string(containsString("Todavía nadie ha jugado a esto.")));
        mvc.perform(get("/ranking"))
                .andExpect(content().string(containsString(">Saldo</a>")))
                .andExpect(content().string(containsString("Eva")));
    }

    @Test
    void enLaQuinielaCuentanLosBoletosRepartidos() {
        Evento[] partidos = new Evento[3];
        String[][] equiposPartido = {{"Getafe CF", "Sevilla FC"}, {"Elche CF", "Levante UD"}, {"Real Madrid", "Elche CF"}};
        for (int i = 0; i < 3; i++) {
            partidos[i] = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                    equipos.findByNombre(equiposPartido[i][0]).orElseThrow(),
                    equipos.findByNombre(equiposPartido[i][1]).orElseThrow(), Hora.ahora().plusDays(1).plusHours(i)));
        }
        Quiniela quiniela = quinielas.crear("Jornada", BigDecimal.TEN,
                List.of(partidos[0].getId(), partidos[1].getId(), partidos[2].getId()));
        quinielas.echarBoleto(ANA, quiniela.getId(), List.of(Resultado.LOCAL, Resultado.LOCAL, Resultado.LOCAL));
        quinielas.echarBoleto(EVA, quiniela.getId(), List.of(Resultado.VISITANTE, Resultado.LOCAL, Resultado.LOCAL));
        assertThat(nombres(TipoJuego.QUINIELA, CriterioRanking.GANANCIAS)).isEmpty();
        for (Evento partido : partidos) {
            resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);
        }
        quinielas.repartir(quiniela.getId());

        assertThat(nombres(TipoJuego.QUINIELA, CriterioRanking.ACIERTOS)).containsExactly("Ana", "Eva");
        // Bote de 18 (20 menos el 10 %) para Ana
        assertThat(de("Ana", TipoJuego.QUINIELA).estadisticas().beneficio()).isEqualByComparingTo("8");
        assertThat(de("Eva", TipoJuego.QUINIELA).estadisticas().beneficio()).isEqualByComparingTo("-10");
    }
}

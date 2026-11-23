package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.CriterioRanking;
import es.ucm.fdi.is1.apuestas.apuesta.RankingService;
import jakarta.persistence.EntityManager;

/** Cuarta depuración: el puesto del perfil con empates y la cabecera de Mis movimientos tras la recarga. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerfilYMovimientosWebTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioService usuarios;
    @Autowired private UsuarioRepository repositorio;
    @Autowired private RankingService ranking;
    @Autowired private EntityManager em;

    @Test
    void elPuestoDelPerfilEsElDelRankingAunqueSeLlamenIgual() {
        // Dos «Ana» y «bea»/«Carla» con el mismo saldo de bienvenida: cada una, su puesto, el mismo que en el ranking
        for (String[] u : new String[][] {{"ana1-perfil@ucm.es", "Ana"}, {"ana2-perfil@ucm.es", "Ana"},
                {"bea-perfil@ucm.es", "bea"}, {"carla-perfil@ucm.es", "Carla"}}) {
            usuarios.crear(u[0], u[1], "secreta123", Rol.USUARIO);
        }
        em.flush();
        // Se compara con lo que enseña /ranking (ordenado en la base de datos): en PostgreSQL, según su configuración,
        // «bea» va antes o después que «Carla», y el perfil tiene que decir lo mismo que esa página
        for (String email : List.of("ana1-perfil@ucm.es", "ana2-perfil@ucm.es", "bea-perfil@ucm.es", "carla-perfil@ucm.es")) {
            Long id = repositorio.findByEmail(email).orElseThrow().getId();
            RankingService.ListaRanking lista = ranking.lista(email, CriterioRanking.SALDO, null);
            int enRanking = Stream.concat(lista.primeros().stream(), Stream.ofNullable(lista.mio()))
                    .filter(p -> p.usuarioId().equals(id)).findFirst().orElseThrow().posicion();
            assertThat(ranking.perfil(id, null).puesto().posicion()).as(email).isEqualTo(enRanking);
        }
    }

    @Test
    void laCabeceraDeMisMovimientosEnsenaElSaldoYaRecargado() throws Exception {
        usuarios.crear("recarga-movs@ucm.es", "Recarga", "secreta123", Rol.USUARIO);
        em.flush();
        em.createNativeQuery("update usuario set ultima_recarga = :antes where email = 'recarga-movs@ucm.es'")
                .setParameter("antes", es.ucm.fdi.is1.apuestas.Hora.ahora().minusDays(8)).executeUpdate();
        em.clear();
        mvc.perform(get("/cuenta/movimientos").with(user("recarga-movs@ucm.es").roles("USUARIO")))
                .andExpect(content().string(containsString("1.200,00 monedas")));
    }
}

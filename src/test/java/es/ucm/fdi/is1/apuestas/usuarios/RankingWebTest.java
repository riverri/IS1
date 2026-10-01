package es.ucm.fdi.is1.apuestas.usuarios;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** HU-36: ranking de usuarios. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RankingWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private UsuarioRepository repositorio;

    @Test
    void ordenaPorSaldoYNoIncluyeAlCreador() throws Exception {
        usuarios.crear("rica@ucm.es", "Rica", "secreta123", Rol.USUARIO);
        repositorio.findByEmail("rica@ucm.es").orElseThrow().abonar(new BigDecimal("500"));

        mvc.perform(get("/ranking"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesRegex("(?s).*Rica.*Usuario de prueba.*")))
                .andExpect(content().string(not(containsString("Creador de apuestas</span>"))));
    }

    @Test
    @WithMockUser(username = "usuario@apuestas.es")
    void destacaAlUsuarioQueLoConsulta() throws Exception {
        mvc.perform(get("/ranking"))
                .andExpect(content().string(containsString("soy-yo")))
                .andExpect(content().string(containsString("(tú)")));
    }
}

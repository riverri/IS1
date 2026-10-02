package es.ucm.fdi.is1.apuestas.ligas;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * La lista de ligas se pinta fuera de la transacción, como en la aplicación real.
 * Sin @Transactional en la prueba: si los miembros no se cargan con la liga, la página falla.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LigasSinTransaccionTest {

    private static final String EMAIL = "liga-sin-transaccion@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private LigaService ligas;

    @Autowired
    private LigaRepository ligaRepository;

    @AfterEach
    void limpiar() {
        ligaRepository.findAll().stream().filter(l -> l.getCreador().getEmail().equals(EMAIL))
                .forEach(ligaRepository::delete);
        usuarios.findByEmail(EMAIL).ifPresent(usuarios::delete);
    }

    @Test
    void laListaDeLigasMuestraSusMiembros() throws Exception {
        usuarioService.crear(EMAIL, "Marta", "secreta123", Rol.USUARIO);
        ligas.crear(EMAIL, "Sin transacción");

        mvc.perform(get("/ligas").with(user(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1 miembro ·")));
    }
}

package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Doble clic en «Entrar» cuando toca la recarga gratuita: la segunda petición choca con la primera (@Version) y ya no
 * acaba en un error 500 con la sesión iniciada; entra igual, porque la recarga ya la ha aplicado la otra.
 */
class RecargaAlEntrarHandlerTest {

    @Test
    void siLaOtraPeticionYaAplicoLaRecargaSeEntraIgual() throws Exception {
        UsuarioService usuarios = mock(UsuarioService.class);
        when(usuarios.consultarConRecarga("ana@ucm.es"))
                .thenThrow(new ObjectOptimisticLockingFailureException(Usuario.class, 1L));
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        new RecargaAlEntrarHandler(usuarios).onAuthenticationSuccess(new MockHttpServletRequest(), respuesta,
                new UsernamePasswordAuthenticationToken("ana@ucm.es", null, java.util.List.of()));

        assertThat(respuesta.getRedirectedUrl()).isEqualTo("/eventos");
    }
}

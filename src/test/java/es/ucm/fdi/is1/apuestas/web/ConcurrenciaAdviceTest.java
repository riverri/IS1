package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

/** El aviso de "dos operaciones a la vez" depende de quién lo recibe: un jugador o el creador en Gestión. */
class ConcurrenciaAdviceTest {

    private final ConcurrenciaAdvice advice = new ConcurrenciaAdvice();

    private static MockHttpServletRequest peticion(String ruta) {
        return new MockHttpServletRequest("POST", ruta);
    }

    @Test
    void alJugadorLeHablaDeSuSaldo() {
        RedirectAttributesModelMap flash = new RedirectAttributesModelMap();

        String destino = advice.aLaVez(null, peticion("/apuestas/7/cancelar"), flash);

        assertThat(destino).isEqualTo("redirect:/apuestas");
        assertThat(flash.getFlashAttributes().get("error")).isEqualTo(ConcurrenciaAdvice.AVISO_JUGADOR);
    }

    @Test
    void alCreadorLeExplicaQueUnApostanteEstabaOperando() {
        RedirectAttributesModelMap flash = new RedirectAttributesModelMap();

        String destino = advice.aLaVez("http://localhost/gestion/eventos/3", peticion("/gestion/eventos/3/resultado"),
                flash);

        assertThat(destino).isEqualTo("redirect:/gestion/eventos/3");
        assertThat(flash.getFlashAttributes().get("error")).isEqualTo(ConcurrenciaAdvice.AVISO_GESTION);
    }
}

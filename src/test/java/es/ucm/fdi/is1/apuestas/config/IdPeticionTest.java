package es.ucm.fdi.is1.apuestas.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Identificador de petición en la respuesta y en el registro mientras se atiende. */
class IdPeticionTest {

    private final IdPeticion filtro = new IdPeticion();

    /** Atiende una petición y devuelve {id en la respuesta, id en el registro mientras se atendía}. */
    private String[] atender(String cabecera) throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/eventos");
        if (cabecera != null) {
            peticion.addHeader(IdPeticion.CABECERA, cabecera);
        }
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        AtomicReference<String> enElRegistro = new AtomicReference<>();
        filtro.doFilter(peticion, respuesta, (p, r) -> enElRegistro.set(MDC.get(IdPeticion.CLAVE)));
        return new String[] {respuesta.getHeader(IdPeticion.CABECERA), enElRegistro.get()};
    }

    @Test
    void generaUnoSiNoLlegaYLoQuitaAlAcabar() throws Exception {
        String[] id = atender(null);
        assertThat(id[0]).matches("[0-9a-f]{12}").isEqualTo(id[1]);
        assertThat(MDC.get(IdPeticion.CLAVE)).isNull();
        assertThat(atender(null)[0]).isNotEqualTo(id[0]);
    }

    @Test
    void usaElQueTraeLaPeticionSiEsValido() throws Exception {
        assertThat(atender("abc-123-XYZ")).containsExactly("abc-123-XYZ", "abc-123-XYZ");
    }

    @Test
    void noAdmiteSaltosDeLineaNiTextosLargos() throws Exception {
        assertThat(atender("x\nERROR falso")[0]).matches("[0-9a-f]{12}");
        assertThat(atender("a".repeat(65))[0]).matches("[0-9a-f]{12}");
        assertThat(atender("")[0]).matches("[0-9a-f]{12}");
    }
}

package es.ucm.fdi.is1.apuestas.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * F-04: en los perfiles públicos (compartir, nube) la contraseña del creador es la de CREADOR_PASSWORD,
 * aunque el creador ya existiera con la del README.
 */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:password-fija",
        "apuestas.creador.password=OtraClave2026", "apuestas.creador.password-fija=true"})
class PasswordCreadorFijaTest {

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private DatosIniciales datos;

    @Test
    void laContrasenaPublicaYaNoSirve() throws Exception {
        usuarios.fijarPassword("creador@apuestas.es", "creador123");
        datos.run(null);

        assertThat(usuarios.passwordCorrecta("creador@apuestas.es", "creador123")).isFalse();
        assertThat(usuarios.passwordCorrecta("creador@apuestas.es", "OtraClave2026")).isTrue();
    }
}

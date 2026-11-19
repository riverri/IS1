package es.ucm.fdi.is1.apuestas.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Perfil compartir: la base de datos es la local, donde el usuario de prueba ya existe con la contraseña pública del
 * README. Sin USUARIO_PASSWORD deja de servir (se le pone una al azar); con ella, manda la de la variable.
 */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:password-usuario-fija",
        "apuestas.usuario.password=", "apuestas.usuario.password-fija=true"})
class PasswordUsuarioFijaTest {

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private DatosIniciales datos;

    @Test
    void sinUsuarioPasswordLaContrasenaDelReadmeYaNoSirve() throws Exception {
        // Como en una base local creada antes en desarrollo
        usuarios.crear("usuario@apuestas.es", "Usuario de prueba", "usuario123",
                es.ucm.fdi.is1.apuestas.usuarios.Rol.USUARIO);
        datos.run(null);

        assertThat(usuarios.passwordCorrecta("usuario@apuestas.es", "usuario123")).isFalse();
    }
}

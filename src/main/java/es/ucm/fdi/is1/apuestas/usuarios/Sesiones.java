package es.ucm.fdi.is1.apuestas.usuarios;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;

/**
 * Cierra las sesiones de un usuario en otros navegadores al eliminar la cuenta o cambiar la contraseña:
 * si no, seguían abiertas (y después de una baja solo daban errores).
 * Se usa en la baja (HU-18), el cambio de contraseña (HU-47) y la recuperación (HU-17).
 * El registro de sesiones lo configura {@code SeguridadConfig}.
 */
@Component
public class Sesiones {

    private final SessionRegistry registro;
    private final JdbcNavegadorTokenRepository navegadores;
    private final CambioCorreoPendienteRepository cambiosCorreo;

    public Sesiones(SessionRegistry registro, JdbcNavegadorTokenRepository navegadores,
                    CambioCorreoPendienteRepository cambiosCorreo) {
        this.registro = registro;
        this.navegadores = navegadores;
        this.cambiosCorreo = cambiosCorreo;
    }

    /**
     * Todas las del usuario menos {@code actual} (null = todas), los navegadores recordados y los cambios de correo
     * pendientes: si alguien que sabía la contraseña pidió cambiar el correo, cambiar la contraseña tiene que anularlo.
     */
    public void cerrarOtras(String email, HttpSession actual) {
        navegadores.removeUserTokens(email);
        cambiosCorreo.borrarDeEmail(email);
        String id = actual == null ? null : actual.getId();
        // Cada principal es un usuario con sesión; false = sin contar las que ya han caducado
        registro.getAllPrincipals().stream()
                // "p instanceof UserDetails u": si p es un UserDetails, lo usa ya convertido con el nombre u
                .filter(p -> p instanceof UserDetails u && u.getUsername().equalsIgnoreCase(email))
                // flatMap: cada usuario tiene una lista de sesiones; se juntan todas en un único stream
                .flatMap(p -> registro.getAllSessions(p, false).stream())
                .filter(s -> !s.getSessionId().equals(id))
                // expireNow la marca como caducada: en su siguiente petición Spring la cierra y lleva a /login?expirada
                .forEach(s -> s.expireNow());
    }
}

package es.ucm.fdi.is1.apuestas.usuarios;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;

/**
 * Cierra las sesiones de un usuario en otros navegadores al eliminar la cuenta o cambiar la contraseña:
 * si no, seguían abiertas (y después de una baja solo daban errores).
 */
@Component
public class Sesiones {

    private final SessionRegistry registro;

    public Sesiones(SessionRegistry registro) {
        this.registro = registro;
    }

    /** Todas las del usuario menos {@code actual} (null = todas). */
    public void cerrarOtras(String email, HttpSession actual) {
        String id = actual == null ? null : actual.getId();
        registro.getAllPrincipals().stream()
                .filter(p -> p instanceof UserDetails u && u.getUsername().equalsIgnoreCase(email))
                .flatMap(p -> registro.getAllSessions(p, false).stream())
                .filter(s -> !s.getSessionId().equals(id))
                .forEach(s -> s.expireNow());
    }
}

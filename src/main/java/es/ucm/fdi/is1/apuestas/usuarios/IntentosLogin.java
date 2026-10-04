package es.ucm.fdi.is1.apuestas.usuarios;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Freno a quien prueba contraseñas: tras {@value #MAXIMO_FALLOS} intentos fallidos seguidos con un email,
 * ese email no puede entrar durante 15 minutos (aunque acierte). Se guarda en memoria: al reiniciar se olvida.
 */
@Component
public class IntentosLogin {

    static final int MAXIMO_FALLOS = 5;
    static final Duration BLOQUEO = Duration.ofMinutes(15);

    private record Fallos(int seguidos, Instant ultimo) {
    }

    private final Map<String, Fallos> fallos = new ConcurrentHashMap<>();
    private final Clock reloj;

    public IntentosLogin(Clock reloj) {
        this.reloj = reloj;
    }

    @EventListener
    public void alFallar(AuthenticationFailureBadCredentialsEvent evento) {
        Instant ahora = reloj.instant();
        fallos.merge(clave(evento.getAuthentication().getName()), new Fallos(1, ahora), (antes, nuevo) ->
                caducado(antes, ahora) ? nuevo : new Fallos(antes.seguidos() + 1, ahora));
    }

    @EventListener
    public void alEntrar(AuthenticationSuccessEvent evento) {
        fallos.remove(clave(evento.getAuthentication().getName()));
    }

    public boolean bloqueado(String email) {
        Fallos f = fallos.get(clave(email));
        return f != null && f.seguidos() >= MAXIMO_FALLOS && !caducado(f, reloj.instant());
    }

    private static boolean caducado(Fallos f, Instant ahora) {
        return f.ultimo().plus(BLOQUEO).isBefore(ahora);
    }

    private static String clave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}

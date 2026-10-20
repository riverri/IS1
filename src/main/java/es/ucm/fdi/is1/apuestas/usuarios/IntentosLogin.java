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
 * Escucha los eventos de inicio de sesión que publica Spring Security, y {@link UsuarioDetailsService}
 * le pregunta si la cuenta está bloqueada (HU-12).
 */
@Component
public class IntentosLogin {

    static final int MAXIMO_FALLOS = 5;
    static final Duration BLOQUEO = Duration.ofMinutes(15);

    /** Fallos seguidos de un email y momento del último. */
    private record Fallos(int seguidos, Instant ultimo) {
    }

    // Concurrente: pueden llegar varios inicios de sesión a la vez
    private final Map<String, Fallos> fallos = new ConcurrentHashMap<>();
    private final Clock reloj;

    public IntentosLogin(Clock reloj) {
        this.reloj = reloj;
    }

    /**
     * Email o contraseña incorrectos: suma un fallo. Si el anterior fue hace más de 15 minutos, la cuenta
     * empieza de nuevo. Con la cuenta ya bloqueada Spring publica otro evento, así que esos intentos
     * no alargan el bloqueo.
     */
    @EventListener
    public void alFallar(AuthenticationFailureBadCredentialsEvent evento) {
        Instant ahora = reloj.instant();
        // merge: si no había fallos guarda el nuevo; si los había, los combina con la lambda
        fallos.merge(clave(evento.getAuthentication().getName()), new Fallos(1, ahora), (antes, nuevo) ->
                caducado(antes, ahora) ? nuevo : new Fallos(antes.seguidos() + 1, ahora));
    }

    /** Un inicio de sesión correcto borra los fallos acumulados. */
    @EventListener
    public void alEntrar(AuthenticationSuccessEvent evento) {
        fallos.remove(clave(evento.getAuthentication().getName()));
    }

    /** Tras recuperar la contraseña (HU-17) se olvidan los fallos: si no, seguiría bloqueado con la nueva. */
    public void olvidar(String email) {
        fallos.remove(clave(email));
    }

    /** Lleva {@value #MAXIMO_FALLOS} fallos o más y el último fue hace menos de 15 minutos. */
    public boolean bloqueado(String email) {
        Fallos f = fallos.get(clave(email));
        return f != null && f.seguidos() >= MAXIMO_FALLOS && !caducado(f, reloj.instant());
    }

    /** Han pasado más de {@link #BLOQUEO} desde el último fallo. */
    private static boolean caducado(Fallos f, Instant ahora) {
        return f.ultimo().plus(BLOQUEO).isBefore(ahora);
    }

    /** El email normalizado, para que "Ana@x.es" y "ana@x.es" cuenten juntos. */
    private static String clave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}

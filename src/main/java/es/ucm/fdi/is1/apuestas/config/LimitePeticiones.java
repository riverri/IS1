package es.ucm.fdi.is1.apuestas.config;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Límite de envíos por dirección IP en los formularios sin sesión que crean cosas o mandan correos: registrarse
 * y pedir el correo para recuperar la contraseña. Sin él, un programa podría crear miles de cuentas o llenar el
 * buzón de alguien de correos de recuperación. Entrar con contraseña ya tiene su propio bloqueo (5 fallos).
 *
 * <p>Ventana deslizante: se guardan las horas de los últimos envíos de cada IP y, si en la ventana ya hay
 * {@code maximo}, se responde 429 (demasiadas peticiones) con la cabecera Retry-After. En el servidor, la IP
 * real del visitante la da {@code server.forward-headers-strategy=framework} (perfiles nube y compartir).
 * Con {@code apuestas.limite-peticiones.maximo=0} no hay límite (las pruebas registran muchos usuarios seguidos).
 */
@Component
public class LimitePeticiones extends OncePerRequestFilter {

    /** Formularios limitados (solo los POST: ver la página no cuenta). */
    static final Set<String> RUTAS = Set.of("/registro", "/recuperar", "/verificar-correo");

    /** Con más IP distintas guardadas se vacía el registro, para que no crezca sin fin. */
    private static final int MAXIMO_IPS = 10_000;

    private final int maximo;
    private final Duration ventana;
    private final Clock reloj;
    private final Map<String, Deque<Instant>> envios = new ConcurrentHashMap<>();

    public LimitePeticiones(@Value("${apuestas.limite-peticiones.maximo:10}") int maximo,
                            @Value("${apuestas.limite-peticiones.ventana:PT15M}") Duration ventana, Clock reloj) {
        this.maximo = maximo;
        this.ventana = ventana;
        this.reloj = reloj;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest peticion) {
        return maximo <= 0 || !"POST".equals(peticion.getMethod()) || !RUTAS.contains(ruta(peticion));
    }

    /**
     * La ruta como la ve Spring MVC: descodificada y sin {@code ;...}. Con la URI tal cual llega, {@code /regis%74ro}
     * no contaba y llegaba igual al formulario de registro.
     */
    private static String ruta(HttpServletRequest peticion) {
        return UrlPathHelper.defaultInstance.getPathWithinApplication(peticion);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        if (!admite(peticion.getRemoteAddr() + " " + ruta(peticion))) {
            respuesta.setHeader("Retry-After", String.valueOf(ventana.toSeconds()));
            respuesta.sendError(429, "Demasiados intentos");
            return;
        }
        cadena.doFilter(peticion, respuesta);
    }

    /** Apunta un envío de esa IP a esa ruta si aún cabe en la ventana. */
    boolean admite(String clave) {
        Instant ahora = reloj.instant();
        if (envios.size() > MAXIMO_IPS) {
            envios.clear();
        }
        Deque<Instant> horas = envios.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (horas) {
            while (!horas.isEmpty() && !horas.peekFirst().isAfter(ahora.minus(ventana))) {
                horas.pollFirst();
            }
            if (horas.size() >= maximo) {
                return false;
            }
            horas.addLast(ahora);
            return true;
        }
    }
}

package es.ucm.fdi.is1.apuestas.config;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Identificador de cada petición, para seguirla en el registro del servidor. Va en la respuesta (cabecera
 * X-Request-Id) y en cada línea que escribe esa petición en el registro ({@code logging.pattern.level}).
 * Así, si un jugador dice «me ha salido un error», con el identificador se encuentran las líneas de esa petición
 * entre las de todos los demás.
 *
 * <p>Si la petición ya trae uno (lo pone el servidor de Render, por ejemplo), se usa ese, siempre que sea corto y
 * solo tenga letras, números y guiones: así nadie puede meter saltos de línea falsos en el registro. Si no, se
 * genera uno nuevo. Va el primero de todos los filtros, para que también lo lleven las líneas de Spring Security.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IdPeticion extends OncePerRequestFilter {

    static final String CABECERA = "X-Request-Id";
    /** Clave en el MDC: el contexto del registro de cada hilo, que el patrón lee con %X{peticion}. */
    static final String CLAVE = "peticion";
    private static final Pattern VALIDO = Pattern.compile("[A-Za-z0-9-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String id = peticion.getHeader(CABECERA);
        if (id == null || !VALIDO.matcher(id).matches()) {
            // 12 caracteres bastan para no repetirse entre las peticiones de un día
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        MDC.put(CLAVE, id);
        respuesta.setHeader(CABECERA, id);
        try {
            cadena.doFilter(peticion, respuesta);
        } finally {
            // El hilo atiende después otras peticiones: no debe quedarse con este identificador
            MDC.remove(CLAVE);
        }
    }
}

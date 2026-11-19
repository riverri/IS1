package es.ucm.fdi.is1.apuestas.web;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Cambia entre los colores claros y el modo oscuro (HU-94). La elección se guarda en la cookie {@value #COOKIE}
 * durante un año, así vale también sin iniciar sesión y se recuerda en las siguientes visitas. La lee
 * {@link UsuarioActualAdvice#temaOscuro}.
 */
@Controller
public class TemaController {

    static final String COOKIE = "tema";
    static final String OSCURO = "oscuro";

    /**
     * GET /tema: pasa al otro tema y vuelve a la página en la que estaba. Es un enlace y no un formulario: un
     * formulario necesita el código CSRF, que crearía una sesión a cada visitante en todas las páginas. Cambiar los
     * colores no toca la cuenta, así que no hace falta protegerlo.
     */
    @GetMapping("/tema")
    public String cambiar(@CookieValue(name = COOKIE, required = false) String actual,
                          @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer,
                          HttpServletResponse respuesta) {
        String nuevo = OSCURO.equals(actual) ? "claro" : OSCURO;
        ResponseCookie cookie = ResponseCookie.from(COOKIE, nuevo).path("/").maxAge(Duration.ofDays(365))
                .httpOnly(true).sameSite("Lax").build();
        respuesta.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        String destino = RutaSegura.de(referer, "/");
        // Tras un formulario de Mi cuenta con errores, la página se pinta en la dirección del POST (/cuenta/nombre,
        // /cuenta/limites…), que no tiene GET: volver ahí daba un error 405. Se vuelve a Mi cuenta
        if (destino.startsWith("/cuenta/")) {
            destino = "/cuenta";
        }
        return "redirect:" + destino;
    }
}

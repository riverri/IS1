package es.ucm.fdi.is1.apuestas.anuncios;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cuenta la impresión de un anuncio (HU-62) solo cuando la página que lo lleva se va a pintar. {@link AnunciosAdvice}
 * lo elige para todas las peticiones GET, también para las que no pintan una página: una descarga CSV, una
 * redirección o una que acaba en error. Esas no deben contar, porque nadie ha visto el anuncio.
 */
@Configuration
public class ImpresionesAnuncio implements WebMvcConfigurer, HandlerInterceptor {

    private final AnuncioService anuncios;

    public ImpresionesAnuncio(AnuncioService anuncios) {
        this.anuncios = anuncios;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(this);
    }

    /** Después del controlador y antes de pintar: si hay una vista (no una redirección) con anuncio, cuenta. */
    @Override
    public void postHandle(HttpServletRequest peticion, HttpServletResponse respuesta, Object controlador,
                           ModelAndView vista) {
        if (vista == null || vista.getViewName() == null || vista.getViewName().startsWith("redirect:")
                || vista.getViewName().startsWith("forward:")) {
            return;
        }
        if (vista.getModel().get("anuncio") instanceof Anuncio anuncio) {
            anuncios.contarImpresion(anuncio.getId());
        }
    }
}

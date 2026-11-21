package es.ucm.fdi.is1.apuestas.anuncios;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Pone en el modelo de cada página el anuncio que le toca (HU-60), igual que {@code UsuarioActualAdvice} pone el
 * usuario. La cabecera común ({@code fragments/comun.html}) lo pinta con el fragmento {@code fragments/anuncio.html},
 * así ninguna plantilla tiene que ocuparse de los anuncios.
 */
@ControllerAdvice
public class AnunciosAdvice {

    private final AnuncioService anuncios;

    public AnunciosAdvice(AnuncioService anuncios) {
        this.anuncios = anuncios;
    }

    /**
     * Solo en las peticiones GET. La impresión no se cuenta aquí sino en {@link ImpresionesAnuncio}, cuando la página
     * se llega a pintar. El creador de apuestas no juega, así que no ve anuncios.
     */
    @ModelAttribute("anuncio")
    public Anuncio anuncio(HttpServletRequest peticion, Principal principal) {
        if (!"GET".equals(peticion.getMethod()) || peticion.isUserInRole("CREADOR")) {
            return null;
        }
        return anuncios.elegirPara(peticion.getRequestURI(), principal != null).orElse(null);
    }
}

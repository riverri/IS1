package es.ucm.fdi.is1.apuestas.anuncios;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Clic en un anuncio (HU-60, HU-62). Es un enlace normal: apunta el clic y lleva a la sección. La ruta es pública
 * ({@code SeguridadConfig}) porque los visitantes también ven anuncios.
 */
@Controller
public class AnunciosController {

    private final AnuncioService anuncios;

    public AnunciosController(AnuncioService anuncios) {
        this.anuncios = anuncios;
    }

    /**
     * GET /anuncios/{id}/ir?desde=/eventos: registra el clic y redirige al destino del anuncio. El destino lo pone el
     * creador y siempre es una ruta de esta web ({@link Anuncio#cambiar}), así que no hay redirecciones a otros sitios.
     */
    @GetMapping("/anuncios/{id}/ir")
    public String ir(@PathVariable Long id, @RequestParam(required = false) String desde, Principal principal) {
        return "redirect:" + anuncios.registrarClic(id, desde, principal == null ? null : principal.getName());
    }
}

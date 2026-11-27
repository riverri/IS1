package es.ucm.fdi.is1.apuestas.anuncios;

import java.security.Principal;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Clic en un anuncio (HU-60, HU-62). Es un enlace normal: apunta el clic y lleva a la sección. Las precargas del
 * navegador y los rastreadores también siguen el enlace, pero no cuentan como clic. La ruta es pública
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
    public String ir(@PathVariable Long id, @RequestParam(required = false) String desde, Principal principal,
                     HttpServletRequest peticion) {
        if (noLoHaPulsadoNadie(peticion)) {
            return "redirect:" + anuncios.destino(id);
        }
        return "redirect:" + anuncios.registrarClic(id, desde, principal == null ? null : principal.getName());
    }

    /**
     * Una precarga del navegador (cabeceras {@code Sec-Purpose}, {@code Purpose} o {@code X-Moz}) o un rastreador pide
     * el enlace sin que nadie lo haya pulsado: lleva al mismo sitio, pero no cuenta como clic (HU-62).
     */
    static boolean noLoHaPulsadoNadie(HttpServletRequest peticion) {
        String proposito = (Objects.toString(peticion.getHeader("Sec-Purpose"), "") + " "
                + Objects.toString(peticion.getHeader("Purpose"), "") + " "
                + Objects.toString(peticion.getHeader("X-Moz"), "")).toLowerCase(Locale.ROOT);
        if (proposito.contains("prefetch") || proposito.contains("prerender")) {
            return true;
        }
        String agente = Objects.toString(peticion.getHeader("User-Agent"), "").toLowerCase(Locale.ROOT);
        return agente.contains("bot") || agente.contains("crawler") || agente.contains("spider");
    }
}

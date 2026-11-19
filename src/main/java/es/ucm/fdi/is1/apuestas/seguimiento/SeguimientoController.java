package es.ucm.fdi.is1.apuestas.seguimiento;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

/** Seguir equipos (HU-86) y alertas de cuota (HU-90). Todo exige sesión ({@code SeguridadConfig}). */
@Controller
public class SeguimientoController {

    private final FavoritosService favoritos;
    private final AlertasService alertas;

    public SeguimientoController(FavoritosService favoritos, AlertasService alertas) {
        this.favoritos = favoritos;
        this.alertas = alertas;
    }

    /** POST /equipos/{id}/favorito: lo marca o lo quita de favoritos y vuelve a su ficha. */
    @PostMapping("/equipos/{id}/favorito")
    public String favorito(@PathVariable Long id, Principal principal, RedirectAttributes redireccion) {
        try {
            boolean ahora = favoritos.alternar(principal.getName(), id);
            redireccion.addFlashAttribute("mensaje", ahora
                    ? "Añadido a tus favoritos: verás sus partidos los primeros y te avisaremos de los nuevos."
                    : "Quitado de tus favoritos.");
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            // Doble clic: la otra petición lo acaba de marcar. Ya es favorito (sin esto, error 500)
            redireccion.addFlashAttribute("mensaje", "Añadido a tus favoritos.");
        }
        return "redirect:/equipos/" + id;
    }

    /** POST /eventos/{id}/alertas: avísame cuando la cuota de ese resultado llegue a la que pongo. */
    @PostMapping("/eventos/{id}/alertas")
    public String crearAlerta(@PathVariable Long id, @RequestParam(required = false) Resultado resultado,
                              @RequestParam(required = false) BigDecimal cuota, Principal principal,
                              RedirectAttributes redireccion) {
        try {
            alertas.crear(principal.getName(), id, resultado, cuota);
            redireccion.addFlashAttribute("mensajeAlerta", "Alerta guardada: te avisaremos cuando la cuota llegue a "
                    + cuota.toPlainString().replace('.', ',') + ".");
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("errorAlerta", e.getMessage());
        } catch (IllegalStateException e) {
            // Ya no admite apuestas: su página daría 404 y el aviso no se vería. Al catálogo, con el aviso
            redireccion.addFlashAttribute("errorBoleto", e.getMessage());
            return "redirect:/eventos";
        }
        return "redirect:/eventos/" + id + "/apostar#alertas";
    }

    /** POST /alertas/{id}/borrar. */
    @PostMapping("/alertas/{id}/borrar")
    public String borrarAlerta(@PathVariable Long id, Principal principal, RedirectAttributes redireccion) {
        try {
            Long evento = alertas.borrar(principal.getName(), id);
            redireccion.addFlashAttribute("mensajeAlerta", "Alerta borrada.");
            return "redirect:/eventos/" + evento + "/apostar#alertas";
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/apuestas";
        }
    }
}

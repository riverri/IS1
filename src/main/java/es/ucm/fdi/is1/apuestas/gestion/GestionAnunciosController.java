package es.ucm.fdi.is1.apuestas.gestion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.anuncios.Anuncio;
import es.ucm.fdi.is1.apuestas.anuncios.AnuncioService;
import es.ucm.fdi.is1.apuestas.anuncios.Ubicacion;
import jakarta.validation.Valid;

/**
 * Anuncios internos en el panel del creador (HU-61, HU-62): lista con impresiones, clics y porcentaje de clics,
 * alta, edición y activar o desactivar. Sus rutas cuelgan de {@code /gestion/anuncios}, así que solo las usa el rol
 * CREADOR (ver SeguridadConfig). Mismo patrón de formularios que {@link GestionController}.
 */
@Controller
@RequestMapping("/gestion/anuncios")
public class GestionAnunciosController {

    private final AnuncioService anuncios;

    public GestionAnunciosController(AnuncioService anuncios) {
        this.anuncios = anuncios;
    }

    /** Secciones para las casillas del formulario. */
    @ModelAttribute("ubicaciones")
    public Ubicacion[] ubicaciones() {
        return Ubicacion.values();
    }

    /** GET /gestion/anuncios: todos los anuncios con sus cifras (CA-62.1). */
    @GetMapping
    public String lista(Model model) {
        model.addAttribute("estadisticas", anuncios.estadisticas());
        return "gestion/anuncios";
    }

    /** GET /gestion/anuncios/nuevo: formulario vacío. */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("datosAnuncio", new AnuncioForm());
        model.addAttribute("accion", "/gestion/anuncios/nuevo");
        return "gestion/anuncio";
    }

    /** POST /gestion/anuncios/nuevo: crea el anuncio (CA-61.1) o vuelve al formulario con el error (CA-61.2). */
    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("datosAnuncio") AnuncioForm form, BindingResult errores, Model model,
                        RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                Anuncio anuncio = anuncios.crear(form.getTitulo(), form.getTexto(), form.getIcono(), form.getDestino(),
                        form.getUbicaciones(), form.getPrioridad(), form.getDesde(), form.getHasta());
                redireccion.addFlashAttribute("mensaje", "Anuncio creado: " + anuncio.getTitulo());
                return "redirect:/gestion/anuncios";
            } catch (IllegalArgumentException e) {
                // Reglas entre campos (destino, fechas): se muestran encima del formulario
                errores.reject("invalido", e.getMessage());
            }
        }
        model.addAttribute("accion", "/gestion/anuncios/nuevo");
        return "gestion/anuncio";
    }

    /** GET /gestion/anuncios/{id}/editar: formulario con los datos del anuncio. */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("datosAnuncio", AnuncioForm.de(anuncios.anuncio(id)));
        model.addAttribute("accion", "/gestion/anuncios/" + id + "/editar");
        return "gestion/anuncio";
    }

    /** POST /gestion/anuncios/{id}/editar: guarda los cambios; impresiones y clics se conservan. */
    @PostMapping("/{id}/editar")
    public String guardar(@PathVariable Long id, @Valid @ModelAttribute("datosAnuncio") AnuncioForm form,
                          BindingResult errores, Model model, RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                anuncios.editar(id, form.getTitulo(), form.getTexto(), form.getIcono(), form.getDestino(),
                        form.getUbicaciones(), form.getPrioridad(), form.getDesde(), form.getHasta());
                redireccion.addFlashAttribute("mensaje", "Anuncio actualizado");
                return "redirect:/gestion/anuncios";
            } catch (IllegalArgumentException e) {
                errores.reject("invalido", e.getMessage());
            }
        }
        model.addAttribute("accion", "/gestion/anuncios/" + id + "/editar");
        return "gestion/anuncio";
    }

    /** POST /gestion/anuncios/{id}/activar: lo activa o lo desactiva sin borrar sus estadísticas. */
    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, @RequestParam boolean activo, RedirectAttributes redireccion) {
        Anuncio anuncio = anuncios.activar(id, activo);
        redireccion.addFlashAttribute("mensaje", (activo ? "Anuncio activado: " : "Anuncio desactivado: ")
                + anuncio.getTitulo());
        return "redirect:/gestion/anuncios";
    }
}

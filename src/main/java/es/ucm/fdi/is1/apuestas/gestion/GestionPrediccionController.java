package es.ucm.fdi.is1.apuestas.gestion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.prediccion.Lado;
import es.ucm.fdi.is1.apuestas.prediccion.MercadoPrediccion;
import es.ucm.fdi.is1.apuestas.prediccion.PrediccionService;

/**
 * Mercados de predicción en el panel del creador (HU-63, HU-66): crear, cerrar, anular y marcar la respuesta. Cuelga de
 * {@code /gestion}, así que solo lo usa el rol CREADOR. Las acciones vuelven a la lista con un mensaje o con el error.
 */
@Controller
@RequestMapping("/gestion/predicciones")
public class GestionPrediccionController {

    private final PrediccionService predicciones;

    public GestionPrediccionController(PrediccionService predicciones) {
        this.predicciones = predicciones;
    }

    /** GET /gestion/predicciones: todos los mercados, con su estado y lo que podría perder la casa, y el alta. */
    @GetMapping
    public String lista(Model model) {
        model.addAttribute("mercados", predicciones.todos());
        model.addAttribute("lados", Lado.values());
        return "gestion/predicciones";
    }

    /**
     * POST /gestion/predicciones: crea un mercado abierto con el precio en 0,50 (CA-63.1).
     *
     * @param comision en tanto por ciento (2 = 2 %)
     */
    @PostMapping
    public String crear(@RequestParam(required = false) String pregunta,
                        @RequestParam(required = false) String descripcion,
                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime cierre,
                        @RequestParam(defaultValue = "100") BigDecimal liquidez,
                        @RequestParam(defaultValue = "2") BigDecimal comision, RedirectAttributes redireccion) {
        if (pregunta == null || pregunta.isBlank() || pregunta.length() > 200) {
            redireccion.addFlashAttribute("error", "Escribe la pregunta (como mucho 200 caracteres)");
            return "redirect:/gestion/predicciones";
        }
        try {
            MercadoPrediccion mercado = predicciones.crear(pregunta, descripcion, cierre, liquidez, comision);
            redireccion.addFlashAttribute("mensaje", "Mercado creado: " + mercado.getPregunta());
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/predicciones";
    }

    /** POST /gestion/predicciones/{id}/cerrar: deja de admitir compras y ventas (CA-63.2). */
    @PostMapping("/{id}/cerrar")
    public String cerrar(@PathVariable Long id, RedirectAttributes redireccion) {
        return accion(() -> predicciones.cerrar(id), "Mercado cerrado: ya no admite compras ni ventas.", redireccion);
    }

    /** POST /gestion/predicciones/{id}/anular: devuelve a cada uno lo que puso (CA-63.3). */
    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes redireccion) {
        return accion(() -> predicciones.anular(id), "Mercado anulado. Se ha devuelto lo invertido.", redireccion);
    }

    /** POST /gestion/predicciones/{id}/resolver: marca o corrige la respuesta y paga (CA-66.1, CA-66.2). */
    @PostMapping("/{id}/resolver")
    public String resolver(@PathVariable Long id, @RequestParam Lado respuesta, RedirectAttributes redireccion) {
        return accion(() -> predicciones.resolver(id, respuesta),
                "Respuesta «" + respuesta.getNombre() + "» guardada y posiciones liquidadas.", redireccion);
    }

    private static String accion(Runnable accion, String mensaje, RedirectAttributes redireccion) {
        try {
            accion.run();
            redireccion.addFlashAttribute("mensaje", mensaje);
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/predicciones";
    }
}

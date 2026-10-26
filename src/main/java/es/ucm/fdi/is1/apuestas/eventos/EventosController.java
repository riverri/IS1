package es.ucm.fdi.is1.apuestas.eventos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;

/**
 * Controlador MVC del catálogo público de eventos (HU-19, HU-22). Delega en {@link CatalogoService}
 * y pinta la plantilla Thymeleaf {@code eventos.html}.
 */
@Controller
public class EventosController {

    private final CatalogoService catalogo;

    public EventosController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    /**
     * {@code GET /eventos}: eventos disponibles agrupados por deporte y competición, con pestañas por
     * deporte y buscador.
     *
     * @param deporte pestaña elegida ({@code ?deporte=FUTBOL}); sin él se muestran todos
     * @param q       texto buscado en equipos y competición ({@code ?q=madrid}); opcional
     */
    @GetMapping("/eventos")
    public String catalogo(@RequestParam(required = false) Deporte deporte,
                           @RequestParam(required = false) String q, Model model) {
        // Spring convierte el texto "FUTBOL" de la URL en el valor Deporte.FUTBOL del enum automáticamente
        model.addAttribute("eventosPorDeporte", catalogo.buscar(deporte, q));
        // El recuento de las pestañas no depende de los filtros: siempre se ven todos los deportes
        model.addAttribute("recuento", catalogo.recuentoPorDeporte());
        model.addAttribute("deporteElegido", deporte);
        model.addAttribute("q", q == null ? "" : q.trim());
        return "eventos";
    }
}

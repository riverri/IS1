package es.ucm.fdi.is1.apuestas.eventos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EventosController {

    private final CatalogoService catalogo;

    public EventosController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/eventos")
    public String catalogo(Model model) {
        model.addAttribute("eventosPorDeporte", catalogo.eventosDisponiblesPorDeporte());
        return "eventos";
    }

    /** Requiere sesión iniciada: un visitante es redirigido al login (HU-08). */
    @GetMapping("/eventos/{id}/apostar")
    public String apostar(@PathVariable Long id, Model model) {
        model.addAttribute("evento", catalogo.eventoDisponible(id));
        return "apostar";
    }
}

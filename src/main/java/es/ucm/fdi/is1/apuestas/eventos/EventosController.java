package es.ucm.fdi.is1.apuestas.eventos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;

@Controller
public class EventosController {

    private final CatalogoService catalogo;

    public EventosController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/eventos")
    public String catalogo(@RequestParam(required = false) Deporte deporte,
                           @RequestParam(required = false) String q, Model model) {
        model.addAttribute("eventosPorDeporte", catalogo.buscar(deporte, q));
        model.addAttribute("recuento", catalogo.recuentoPorDeporte());
        model.addAttribute("deporteElegido", deporte);
        model.addAttribute("q", q == null ? "" : q.trim());
        return "eventos";
    }
}

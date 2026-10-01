package es.ucm.fdi.is1.apuestas.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;

@Controller
public class InicioController {

    private final EquipoRepository equipos;

    public InicioController(EquipoRepository equipos) {
        this.equipos = equipos;
    }

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/equipos")
    public String equipos(Model model) {
        model.addAttribute("equipos", equipos.findAllByOrderByDeporteAscNombreAsc());
        return "equipos";
    }
}

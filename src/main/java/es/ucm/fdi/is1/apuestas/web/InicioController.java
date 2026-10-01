package es.ucm.fdi.is1.apuestas.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.CatalogoService;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoProperties;

@Controller
public class InicioController {

    private final EquipoRepository equipos;
    private final CatalogoService catalogo;
    private final SaldoProperties saldo;

    public InicioController(EquipoRepository equipos, CatalogoService catalogo, SaldoProperties saldo) {
        this.equipos = equipos;
        this.catalogo = catalogo;
        this.saldo = saldo;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("proximos", catalogo.proximos(4));
        model.addAttribute("bienvenida", saldo.bienvenida());
        return "index";
    }

    @GetMapping("/equipos")
    public String equipos(Model model) {
        model.addAttribute("equipos", equipos.findAllByOrderByDeporteAscNombreAsc());
        return "equipos";
    }
}

package es.ucm.fdi.is1.apuestas.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.EquiposService;
import es.ucm.fdi.is1.apuestas.eventos.CatalogoService;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoProperties;

@Controller
public class InicioController {

    private final EquiposService equipos;
    private final CatalogoService catalogo;
    private final SaldoProperties saldo;

    public InicioController(EquiposService equipos, CatalogoService catalogo, SaldoProperties saldo) {
        this.equipos = equipos;
        this.catalogo = catalogo;
        this.saldo = saldo;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("proximos", catalogo.proximos(4));
        model.addAttribute("recuento", catalogo.recuentoPorDeporte());
        model.addAttribute("bienvenida", saldo.bienvenida());
        return "index";
    }

    @GetMapping("/equipos")
    public String equipos(@RequestParam(required = false) Deporte deporte,
                          @RequestParam(required = false) Long competicion,
                          @RequestParam(required = false) String q, Model model) {
        model.addAttribute("equiposPorDeporte", equipos.buscar(deporte, competicion, q));
        model.addAttribute("recuento", equipos.recuentoPorDeporte());
        model.addAttribute("deporteElegido", deporte);
        model.addAttribute("competicionElegida", competicion);
        model.addAttribute("competiciones", deporte == null ? List.of() : equipos.competiciones(deporte));
        model.addAttribute("q", q == null ? "" : q.trim());
        return "equipos";
    }
}

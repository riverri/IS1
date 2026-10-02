package es.ucm.fdi.is1.apuestas.ligas;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.apuesta.CriterioRanking;
import es.ucm.fdi.is1.apuestas.apuesta.PuestoRanking;

/** Ligas privadas (HU-51): mis ligas, crear, unirse con un código, ver su ranking, salir y borrar. */
@Controller
@RequestMapping("/ligas")
public class LigasController {

    private final LigaService ligas;

    public LigasController(LigaService ligas) {
        this.ligas = ligas;
    }

    @GetMapping
    public String mias(Principal principal, Model model) {
        model.addAttribute("ligas", ligas.mias(principal.getName()));
        return "ligas";
    }

    @PostMapping
    public String crear(@RequestParam(required = false) String nombre, Principal principal,
                        RedirectAttributes redireccion) {
        try {
            Liga liga = ligas.crear(principal.getName(), nombre);
            redireccion.addFlashAttribute("mensaje", "Liga creada. Pasa el código " + liga.getCodigo()
                    + " a tus amigos para que se unan.");
            return "redirect:/ligas/" + liga.getId();
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/ligas";
        }
    }

    @PostMapping("/unirse")
    public String unirse(@RequestParam(required = false) String codigo, Principal principal,
                         RedirectAttributes redireccion) {
        try {
            Liga liga = ligas.unirse(principal.getName(), codigo);
            redireccion.addFlashAttribute("mensaje", "Te has unido a " + liga.getNombre() + ".");
            return "redirect:/ligas/" + liga.getId();
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/ligas";
        }
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, @RequestParam(defaultValue = "SALDO") CriterioRanking orden,
                      Principal principal, Model model) {
        Liga liga = ligas.ver(id, principal.getName());
        List<PuestoRanking> puestos = ligas.ranking(liga, principal.getName(), orden);
        model.addAttribute("liga", liga);
        model.addAttribute("puestos", puestos);
        model.addAttribute("orden", orden);
        model.addAttribute("criterios", CriterioRanking.values());
        model.addAttribute("soyCreador", liga.getCreador().getEmail().equalsIgnoreCase(principal.getName()));
        return "liga";
    }

    @PostMapping("/{id}/salir")
    public String salir(@PathVariable Long id, Principal principal, RedirectAttributes redireccion) {
        try {
            Liga liga = ligas.salir(id, principal.getName());
            redireccion.addFlashAttribute("mensaje", "Has salido de " + liga.getNombre() + ".");
            return "redirect:/ligas";
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/ligas/" + id;
        }
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id, Principal principal, RedirectAttributes redireccion) {
        try {
            Liga liga = ligas.borrar(id, principal.getName());
            redireccion.addFlashAttribute("mensaje", "Liga " + liga.getNombre() + " borrada.");
            return "redirect:/ligas";
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/ligas/" + id;
        }
    }
}

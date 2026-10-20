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

/**
 * Controlador de las ligas privadas (HU-51): mis ligas, crear, unirse con un código, ver su ranking, salir
 * y borrar. Las rutas cuelgan de {@code /ligas} y exigen haber iniciado sesión (ver SeguridadConfig);
 * el usuario se obtiene del {@link Principal}, cuyo nombre es su email.
 * Los errores de las reglas se muestran como mensaje en la página a la que se redirige.
 */
@Controller
@RequestMapping("/ligas")
public class LigasController {

    private final LigaService ligas;

    public LigasController(LigaService ligas) {
        this.ligas = ligas;
    }

    /** GET /ligas: ligas del usuario, con los formularios para crear una o unirse con un código. */
    @GetMapping
    public String mias(Principal principal, Model model) {
        model.addAttribute("ligas", ligas.mias(principal.getName()));
        return "ligas";
    }

    /** POST /ligas: crea una liga y lleva a su página, con el código que hay que compartir (CA-51.1). */
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

    /** POST /ligas/unirse: entra en la liga del código y lleva a su página (CA-51.2). */
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

    /**
     * GET /ligas/{id}: página de la liga, solo para sus miembros (si no, 404). Muestra el ranking
     * de sus miembros con el criterio de {@code orden} (por defecto, saldo) y el código para invitar.
     */
    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, @RequestParam(defaultValue = "SALDO") CriterioRanking orden,
                      Principal principal, Model model) {
        Liga liga = ligas.ver(id, principal.getName());
        List<PuestoRanking> puestos = ligas.ranking(liga, principal.getName(), orden);
        model.addAttribute("liga", liga);
        model.addAttribute("puestos", puestos);
        model.addAttribute("orden", orden);
        model.addAttribute("criterios", CriterioRanking.values());
        // La vista ofrece "borrar" al creador y "salir" al resto
        model.addAttribute("soyCreador", liga.getCreador().getEmail().equalsIgnoreCase(principal.getName()));
        return "liga";
    }

    /** POST /ligas/{id}/salir: sale de la liga (el creador no puede; tiene que borrarla) (CA-51.3). */
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

    /** POST /ligas/{id}/borrar: borra la liga; solo quien la creó (CA-51.3). */
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

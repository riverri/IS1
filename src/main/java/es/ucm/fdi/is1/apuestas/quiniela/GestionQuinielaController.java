package es.ucm.fdi.is1.apuestas.quiniela;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Gestión de las quinielas para el creador (HU-78 y HU-80): montarlas, repartir el bote y anularlas. Todo
 * /gestion/** exige el rol de creador ({@code SeguridadConfig}, CA-78.3).
 */
@Controller
@RequestMapping("/gestion/quinielas")
public class GestionQuinielaController {

    private final QuinielaService quinielas;

    public GestionQuinielaController(QuinielaService quinielas) {
        this.quinielas = quinielas;
    }

    /** GET /gestion/quinielas: formulario para montar una y la lista de todas. */
    @GetMapping
    public String panel(Model model) {
        model.addAttribute("disponibles", quinielas.partidosDisponibles());
        model.addAttribute("quinielas", quinielas.todas());
        return "gestion/quinielas";
    }

    /** POST /gestion/quinielas: crea la quiniela con los partidos marcados (CA-78.1, CA-78.2). */
    @PostMapping
    public String crear(@RequestParam(required = false) String nombre,
                        @RequestParam(required = false) BigDecimal precio,
                        @RequestParam(name = "eventos", required = false) List<Long> eventos,
                        RedirectAttributes redireccion) {
        try {
            Quiniela quiniela = quinielas.crear(nombre, precio, eventos);
            redireccion.addFlashAttribute("mensaje", "Quiniela «" + quiniela.getNombre() + "» creada con "
                    + quiniela.getPartidos().size() + " partidos.");
        } catch (IllegalArgumentException e) {
            // Lo que había escrito vuelve al formulario, para corregir solo lo que falla
            redireccion.addFlashAttribute("nombreEscrito", nombre);
            redireccion.addFlashAttribute("precioEscrito", precio);
            redireccion.addFlashAttribute("eventosElegidos", eventos == null ? List.of() : eventos);
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/quinielas";
    }

    /** POST /gestion/quinielas/{id}/repartir: reparte el bote (HU-80). */
    @PostMapping("/{id}/repartir")
    public String repartir(@PathVariable Long id, RedirectAttributes redireccion) {
        try {
            Quiniela quiniela = quinielas.repartir(id);
            redireccion.addFlashAttribute("mensaje", "Bote de «" + quiniela.getNombre() + "» repartido.");
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/quinielas";
    }

    /** POST /gestion/quinielas/{id}/anular: anula y devuelve el precio de todos los boletos (CA-80.3). */
    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes redireccion) {
        try {
            Quiniela quiniela = quinielas.anular(id);
            redireccion.addFlashAttribute("mensaje", "Quiniela «" + quiniela.getNombre()
                    + "» anulada: se ha devuelto el precio de los boletos.");
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/quinielas";
    }
}

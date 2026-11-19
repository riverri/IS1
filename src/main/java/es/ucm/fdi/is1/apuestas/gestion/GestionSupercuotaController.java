package es.ucm.fdi.is1.apuestas.gestion;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.apuesta.Supercuota;
import es.ucm.fdi.is1.apuestas.apuesta.SupercuotaService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

/** Supercuotas desde la ficha del evento en Gestión (HU-87). Solo el creador ({@code SeguridadConfig}). */
@Controller
public class GestionSupercuotaController {

    private final SupercuotaService supercuotas;

    public GestionSupercuotaController(SupercuotaService supercuotas) {
        this.supercuotas = supercuotas;
    }

    /** POST /gestion/eventos/{id}/supercuota. */
    @PostMapping("/gestion/eventos/{id}/supercuota")
    public String crear(@PathVariable Long id, @RequestParam(required = false) Resultado resultado,
                        @RequestParam(required = false) BigDecimal cuota,
                        @RequestParam(required = false) BigDecimal importeMaximo,
                        @RequestParam(required = false) Integer horas, RedirectAttributes redireccion) {
        try {
            Supercuota s = supercuotas.crear(id, resultado, cuota, importeMaximo, horas);
            redireccion.addFlashAttribute("mensaje", "Supercuota de " + s.getCuota().toPlainString().replace('.', ',')
                    + " a «" + s.getResultado().getDescripcion() + "» publicada.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/eventos/" + id;
    }

    /** POST /gestion/eventos/{id}/supercuota/quitar. */
    @PostMapping("/gestion/eventos/{id}/supercuota/quitar")
    public String quitar(@PathVariable Long id, RedirectAttributes redireccion) {
        supercuotas.quitar(id);
        redireccion.addFlashAttribute("mensaje", "Supercuota retirada.");
        return "redirect:/gestion/eventos/" + id;
    }
}

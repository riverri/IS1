package es.ucm.fdi.is1.apuestas.gestion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.casino.CasinoService;
import es.ucm.fdi.is1.apuestas.casino.PagoBlackjack;
import jakarta.validation.Valid;

/**
 * Configuración del casino en el panel del creador (HU-59): abrirlo o cerrarlo, límites por apuesta y reglas del
 * blackjack, con la ventaja teórica de cada juego. Cuelga de {@code /gestion}, así que solo la usa el rol CREADOR.
 */
@Controller
@RequestMapping("/gestion/casino")
public class GestionCasinoController {

    private final CasinoService casino;

    public GestionCasinoController(CasinoService casino) {
        this.casino = casino;
    }

    /** Opciones del pago del blackjack para el desplegable. */
    @ModelAttribute("pagos")
    public PagoBlackjack[] pagos() {
        return PagoBlackjack.values();
    }

    /** GET /gestion/casino: formulario con la configuración actual y la ventaja de la casa (CA-59.1). */
    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("datosCasino", CasinoForm.de(casino.configuracion()));
        model.addAttribute("configuracion", casino.configuracion());
        return "gestion/casino";
    }

    /** POST /gestion/casino: guarda la configuración; vale para las partidas nuevas (CA-59.1, CA-59.2). */
    @PostMapping
    public String guardar(@Valid @ModelAttribute("datosCasino") CasinoForm form, BindingResult errores, Model model,
                          RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                casino.cambiarConfiguracion(form.isActivo(), form.getImporteMinimo(), form.getImporteMaximo(),
                        form.getBarajas(), form.isBancaPideCon17Blando(), form.getPagoBlackjack());
                redireccion.addFlashAttribute("mensaje", form.isActivo() ? "Configuración del casino guardada"
                        : "Configuración guardada: el casino queda cerrado");
                return "redirect:/gestion/casino";
            } catch (IllegalArgumentException e) {
                // Reglas entre campos (máximo ≥ mínimo), junto al importe máximo
                errores.rejectValue("importeMaximo", "invalido", e.getMessage());
            }
        }
        model.addAttribute("configuracion", casino.configuracion());
        return "gestion/casino";
    }
}

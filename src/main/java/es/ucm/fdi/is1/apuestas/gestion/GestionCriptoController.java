package es.ucm.fdi.is1.apuestas.gestion;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.cripto.CriptoService;

/**
 * Criptomonedas en el panel del creador (HU-73): abrir o cerrar la sección, la comisión, cuánto sirve un precio, qué
 * criptos se ofrecen y descargar los precios en el momento. Cuelga de {@code /gestion}, así que solo lo usa el rol
 * CREADOR.
 */
@Controller
@RequestMapping("/gestion/cripto")
public class GestionCriptoController {

    private final CriptoService cripto;

    public GestionCriptoController(CriptoService cripto) {
        this.cripto = cripto;
    }

    /** GET /gestion/cripto: la configuración y todas las criptos, también las retiradas. */
    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("configuracion", cripto.configuracion());
        model.addAttribute("criptos", cripto.todas());
        return "gestion/cripto";
    }

    /**
     * POST /gestion/cripto: guarda la configuración (CA-73.1).
     *
     * @param comision en tanto por ciento (1 = 1 %)
     */
    @PostMapping
    public String configurar(@RequestParam(defaultValue = "false") boolean activo,
                             @RequestParam(required = false) BigDecimal comision,
                             @RequestParam(defaultValue = "15") int minutosValidez, RedirectAttributes redireccion) {
        try {
            cripto.configurar(activo, comision, minutosValidez);
            redireccion.addFlashAttribute("mensaje", "Configuración guardada");
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/cripto";
    }

    /** POST /gestion/cripto/{simbolo}/activar: ofrece o retira una cripto; las carteras no se tocan (CA-73.2). */
    @PostMapping("/{simbolo}/activar")
    public String activar(@PathVariable String simbolo, @RequestParam boolean activa, RedirectAttributes redireccion) {
        cripto.activar(simbolo, activa);
        redireccion.addFlashAttribute("mensaje", simbolo + (activa ? " vuelve a ofrecerse" : " retirada: ya no se puede"
                + " comprar, pero quien la tenga puede venderla"));
        return "redirect:/gestion/cripto";
    }

    /** POST /gestion/cripto/actualizar: descarga los precios ahora, sin esperar a la tarea programada (CA-73.3). */
    @PostMapping("/actualizar")
    public String actualizar(RedirectAttributes redireccion) {
        try {
            int cuantas = cripto.actualizarPrecios();
            redireccion.addFlashAttribute("mensaje", "Precios descargados: " + cuantas + " criptomonedas al día");
        } catch (RestClientException e) {
            redireccion.addFlashAttribute("error", "No se ha podido conectar con CoinGecko: " + e.getMessage());
        }
        return "redirect:/gestion/cripto";
    }
}

package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;

/** Boleto de apuestas: añadir selecciones desde el catálogo, revisarlas y confirmar (HU-28, HU-29). */
@Controller
@RequestMapping("/boleto")
public class BoletoController {

    private final BoletoService boletos;
    private final Boleto boleto;

    public BoletoController(BoletoService boletos, Boleto boleto) {
        this.boletos = boletos;
        this.boleto = boleto;
    }

    @GetMapping
    public String ver(Model model) {
        model.addAttribute("vista", boletos.vista(boleto));
        model.addAttribute("ocultarBarraBoleto", true);
        return "boleto";
    }

    /** Se llega pulsando una cuota del catálogo; vuelve a la página de la que se venía. */
    @GetMapping("/anadir")
    public String anadir(@RequestParam("evento") Long eventoId, @RequestParam Resultado resultado,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redireccion) {
        try {
            boletos.anadir(boleto, eventoId, resultado);
            redireccion.addFlashAttribute("mensajeBoleto", "Añadido al boleto");
        } catch (IllegalArgumentException | ResultadoNoValidoException e) {
            redireccion.addFlashAttribute("errorBoleto", e.getMessage());
        }
        return "redirect:" + volver(referer);
    }

    @PostMapping("/quitar/{eventoId}")
    public String quitar(@PathVariable Long eventoId) {
        boletos.quitar(boleto, eventoId);
        return "redirect:/boleto";
    }

    @PostMapping("/confirmar")
    public String confirmar(@RequestParam(required = false) BigDecimal importe, Principal principal,
                            RedirectAttributes redireccion) {
        if (importe == null || importe.compareTo(BigDecimal.ONE) < 0 || importe.scale() > 2) {
            redireccion.addFlashAttribute("error", "Introduce un importe de al menos 1 moneda, con 2 decimales como mucho");
            return "redirect:/boleto";
        }
        try {
            Apuesta apuesta = boletos.confirmar(principal.getName(), boleto, importe);
            redireccion.addFlashAttribute("mensaje", (apuesta.isCombinada() ? "Combinada" : "Apuesta")
                    + " registrada. Si aciertas cobras "
                    + apuesta.getGananciaPotencial().toPlainString().replace('.', ',') + " monedas.");
            return "redirect:/apuestas";
        } catch (CuotasCambiadasException e) {
            redireccion.addFlashAttribute("aviso",
                    "Han cambiado las cuotas de algunas selecciones. Revisa el multiplicador y confirma de nuevo.");
        } catch (SaldoInsuficienteException e) {
            redireccion.addFlashAttribute("error", "No tienes saldo suficiente para esta apuesta");
        } catch (IllegalArgumentException | ResultadoNoValidoException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        redireccion.addFlashAttribute("importe", importe);
        return "redirect:/boleto";
    }

    /** Solo vuelve a páginas de esta misma web; si no, al boleto. */
    private static String volver(String referer) {
        if (referer == null) {
            return "/boleto";
        }
        try {
            URI uri = URI.create(referer);
            String ruta = uri.getRawPath();
            if (ruta == null || !ruta.startsWith("/") || ruta.startsWith("//") || ruta.startsWith("/login")) {
                return "/boleto";
            }
            return uri.getRawQuery() == null ? ruta : ruta + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException e) {
            return "/boleto";
        }
    }
}

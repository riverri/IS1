package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.mercados.MercadoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.mercados.MercadoService;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;

/** Apuestas a largo plazo: Balón de Oro, campeón de liga… (HU-44). La página es pública; apostar exige sesión. */
@Controller
public class MercadosController {

    private final MercadoService mercados;
    private final ApuestaService apuestas;
    private final LimitesService limites;

    public MercadosController(MercadoService mercados, ApuestaService apuestas, LimitesService limites) {
        this.mercados = mercados;
        this.apuestas = apuestas;
        this.limites = limites;
    }

    @GetMapping("/mercados")
    public String mercados(Model model) {
        model.addAttribute("abiertos", mercados.abiertos());
        model.addAttribute("terminados", mercados.terminados());
        model.addAttribute("limitesApuesta", limites.actuales());
        return "mercados";
    }

    @PostMapping("/mercados/{id}/apostar")
    public String apostar(@PathVariable Long id, @RequestParam(required = false) Long candidato,
                          @RequestParam(required = false) BigDecimal cuota,
                          @RequestParam(required = false) BigDecimal importe, Principal principal,
                          RedirectAttributes redireccion) {
        String volver = "redirect:/mercados#mercado-" + id;
        if (candidato == null) {
            redireccion.addFlashAttribute("error", "Elige un candidato");
            return volver;
        }
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            redireccion.addFlashAttribute("error", "Introduce un importe válido, con 2 decimales como mucho");
            return volver;
        }
        try {
            Apuesta apuesta = apuestas.apostarMercado(principal.getName(), id, candidato, cuota, importe);
            redireccion.addFlashAttribute("mensaje", "Apuesta a " + apuesta.getSelecciones().get(0).getDescripcionPronostico()
                    + " registrada. Si aciertas cobras "
                    + apuesta.getGananciaPotencial().toPlainString().replace('.', ',') + " monedas.");
            return "redirect:/apuestas";
        } catch (CuotasCambiadasException e) {
            redireccion.addFlashAttribute("error", "La cuota ha cambiado mientras apostabas. Revísala y vuelve a confirmar.");
        } catch (SaldoInsuficienteException e) {
            redireccion.addFlashAttribute("error", "No tienes saldo suficiente para esta apuesta");
        } catch (MercadoNoDisponibleException | IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return volver;
    }
}

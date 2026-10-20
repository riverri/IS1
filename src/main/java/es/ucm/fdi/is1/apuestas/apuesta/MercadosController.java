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

/**
 * Apuestas a largo plazo: Balón de Oro, campeón de liga… (HU-44). La página es pública; apostar exige sesión.
 * Los mercados los gestiona el creador desde otro controlador del paquete de gestión (HU-45).
 */
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

    /** GET /mercados: los mercados abiertos con sus candidatos y cuotas (CA-44.1) y los ya terminados. */
    @GetMapping("/mercados")
    public String mercados(Model model) {
        model.addAttribute("abiertos", mercados.abiertos());
        model.addAttribute("terminados", mercados.terminados());
        model.addAttribute("limitesApuesta", limites.actuales());
        return "mercados";
    }

    /**
     * POST /mercados/{id}/apostar: apuesta a un candidato (CA-44.2). {@code cuota} es la que veía el usuario;
     * si el creador la ha cambiado, no se apuesta. Si sale bien lleva a Mis apuestas; si no, vuelve al
     * mercado con el error.
     */
    @PostMapping("/mercados/{id}/apostar")
    public String apostar(@PathVariable Long id, @RequestParam(required = false) Long candidato,
                          @RequestParam(required = false) BigDecimal cuota,
                          @RequestParam(required = false) BigDecimal importe, Principal principal,
                          RedirectAttributes redireccion) {
        // El ancla lleva de vuelta al mercado en el que se estaba
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
            // Mercado cerrado (CA-44.3), límites de importe, juego responsable o creador que intenta apostar
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return volver;
    }
}

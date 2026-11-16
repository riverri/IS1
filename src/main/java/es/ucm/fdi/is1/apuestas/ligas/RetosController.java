package es.ucm.fdi.is1.apuestas.ligas;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;
import es.ucm.fdi.is1.apuestas.web.RutaSegura;

/** Retos 1 contra 1 desde la página de una liga (HU-89). Exige sesión ({@code SeguridadConfig}). */
@Controller
public class RetosController {

    private final RetoService retos;

    public RetosController(RetoService retos) {
        this.retos = retos;
    }

    /** POST /retos: reta a un compañero. */
    @PostMapping("/retos")
    public String retar(@RequestParam(required = false) Long retado, @RequestParam(required = false) Long evento,
                        @RequestParam(required = false) Resultado pronostico,
                        @RequestParam(required = false) BigDecimal importe,
                        @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer,
                        Principal principal, RedirectAttributes redireccion) {
        try {
            retos.retar(principal.getName(), retado, evento, pronostico, importe);
            redireccion.addFlashAttribute("mensaje", "Reto enviado. Te avisaremos cuando conteste.");
        } catch (SaldoInsuficienteException e) {
            redireccion.addFlashAttribute("error", "No tienes saldo suficiente para el reto");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + RutaSegura.de(referer, "/ligas");
    }

    /** POST /retos/{id}/aceptar: con un resultado distinto del del retador. */
    @PostMapping("/retos/{id}/aceptar")
    public String aceptar(@PathVariable Long id, @RequestParam(required = false) Resultado pronostico,
                          @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer,
                          Principal principal, RedirectAttributes redireccion) {
        try {
            retos.aceptar(principal.getName(), id, pronostico);
            redireccion.addFlashAttribute("mensaje", "Reto aceptado. ¡Suerte!");
        } catch (SaldoInsuficienteException e) {
            redireccion.addFlashAttribute("error", "No tienes saldo suficiente para aceptar el reto");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + RutaSegura.de(referer, "/ligas");
    }

    /** POST /retos/{id}/rechazar: el retado lo rechaza o el retador lo retira. */
    @PostMapping("/retos/{id}/rechazar")
    public String rechazar(@PathVariable Long id, @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer,
                           Principal principal, RedirectAttributes redireccion) {
        try {
            retos.rechazar(principal.getName(), id);
            redireccion.addFlashAttribute("mensaje", "Reto cancelado: se ha devuelto el importe.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + RutaSegura.de(referer, "/ligas");
    }
}

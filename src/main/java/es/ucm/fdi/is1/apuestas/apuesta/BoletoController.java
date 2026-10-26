package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;
import es.ucm.fdi.is1.apuestas.web.RutaSegura;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Boleto de apuestas: añadir selecciones desde el catálogo, revisarlas y confirmar (HU-28, HU-29).
 * Todas las rutas cuelgan de {@code /boleto} y exigen sesión. La lógica está en {@link BoletoService};
 * aquí se traducen las excepciones a mensajes flash y se redirige.
 */
@Controller
@RequestMapping("/boleto")
public class BoletoController {

    private final BoletoService boletos;
    /** Proxy al boleto de la sesión actual (ver {@link Boleto}). */
    private final Boleto boleto;
    private final LimitesService limites;

    public BoletoController(BoletoService boletos, Boleto boleto, LimitesService limites) {
        this.boletos = boletos;
        this.boleto = boleto;
        this.limites = limites;
    }

    /**
     * GET /boleto: las selecciones con su cuota actual, el multiplicador y los límites de importe.
     * Quita las que ya no admiten apuestas y oculta la barra del boleto, que aquí sobra.
     */
    @GetMapping
    public String ver(Model model) {
        model.addAttribute("vista", boletos.vista(boleto));
        model.addAttribute("ocultarBarraBoleto", true);
        model.addAttribute("limitesApuesta", limites.actuales());
        return "boleto";
    }

    /**
     * Se llega pulsando una cuota del catálogo, o con el botón "Añadir al boleto" de la página del partido,
     * que envía la opción elegida ({@code opcion}); vuelve a la página de la que se venía.
     * Es un POST porque cambia el boleto.
     * POST /boleto/anadir. El creador de apuestas no puede añadir nada.
     */
    @PostMapping("/anadir")
    public String anadir(@RequestParam("evento") Long eventoId, @RequestParam(required = false) Resultado resultado,
                         @RequestParam(required = false) Especial especial,
                         @RequestParam(required = false) String opcion,
                         // Referer: cabecera que manda el navegador con la página desde la que se viene
                         @RequestHeader(value = "Referer", required = false) String referer,
                         HttpServletRequest peticion, RedirectAttributes redireccion) {
        if (peticion.isUserInRole("CREADOR")) {
            redireccion.addFlashAttribute("errorBoleto", "El creador de apuestas no puede apostar");
            return "redirect:" + RutaSegura.de(referer, "/eventos");
        }
        // "opcion" llega del grupo de opciones de la página del partido; se traduce igual que en ApuestaForm
        if (opcion != null) {
            ApuestaForm elegida = new ApuestaForm();
            elegida.setOpcion(opcion);
            resultado = elegida.getResultado();
            especial = elegida.getEspecial();
        }
        try {
            boletos.anadir(boleto, eventoId, resultado, especial);
            redireccion.addFlashAttribute("mensajeBoleto", "Añadido al boleto");
        } catch (IllegalArgumentException | ResultadoNoValidoException e) {
            redireccion.addFlashAttribute("errorBoleto", e.getMessage());
        } catch (EventoNoDisponibleException e) {
            redireccion.addFlashAttribute("errorBoleto", "Ese partido ya no admite apuestas");
        }
        // Solo se vuelve al Referer si es una ruta de esta web (evita redirecciones a otros sitios)
        return "redirect:" + RutaSegura.de(referer, "/boleto");
    }

    /** GET /boleto/anadir: un enlace antiguo (o volver tras iniciar sesión) no añade nada: lleva a la página del partido. */
    @GetMapping("/anadir")
    public String anadirDesdeEnlace(@RequestParam("evento") Long eventoId) {
        return "redirect:/eventos/" + eventoId + "/apostar";
    }

    /** POST /boleto/quitar/{eventoId}: quita la selección de ese partido (CA-28.3). */
    @PostMapping("/quitar/{eventoId}")
    public String quitar(@PathVariable Long eventoId) {
        boletos.quitar(boleto, eventoId);
        return "redirect:/boleto";
    }

    /**
     * POST /boleto/confirmar: convierte el boleto en una apuesta simple o combinada y lleva a Mis apuestas.
     * {@code cuotaVista}: las cuotas que el usuario tenía delante al confirmar ("evento:cuota"). Si alguna ha
     * cambiado desde entonces, no se apuesta y se le enseñan las nuevas (HU-29). Así el botón
     * "Aceptar nuevas cuotas" apuesta a la primera con las cuotas que acaba de ver.
     * Si algo falla, vuelve al boleto con el error y el importe que había escrito.
     */
    @PostMapping("/confirmar")
    public String confirmar(@RequestParam(required = false) BigDecimal importe,
                            @RequestParam(required = false) List<String> cuotaVista, Principal principal,
                            RedirectAttributes redireccion) {
        // Positivo y con 2 decimales como mucho; los límites del creador se comprueban en el servicio
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            redireccion.addFlashAttribute("error", "Introduce un importe válido, con 2 decimales como mucho");
            return "redirect:/boleto";
        }
        if (cuotaVista != null) {
            boletos.fijarCuotasVistas(boleto, cuotaVista);
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
        } catch (EventoNoDisponibleException e) {
            // Se quita del boleto al volver a GET /boleto, que llama a BoletoService.vista
            redireccion.addFlashAttribute("error",
                    "Uno de los partidos ya no admite apuestas (ha empezado o se ha suspendido) y se ha quitado del boleto.");
        }
        redireccion.addFlashAttribute("importe", importe);
        return "redirect:/boleto";
    }

}

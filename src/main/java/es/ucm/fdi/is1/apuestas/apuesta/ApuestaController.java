package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.CatalogoService;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.FichaEquipoService;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;
import jakarta.validation.Valid;

@Controller
public class ApuestaController {

    private final ApuestaService apuestas;
    private final CatalogoService catalogo;
    private final CalculadoraCuotas calculadora;

    private final RankingService rankings;
    private final LimitesService limites;
    private final FichaEquipoService fichas;
    private final Clock reloj;

    public ApuestaController(ApuestaService apuestas, CatalogoService catalogo, CalculadoraCuotas calculadora,
                             RankingService rankings, LimitesService limites, FichaEquipoService fichas,
                             Clock reloj) {
        this.apuestas = apuestas;
        this.catalogo = catalogo;
        this.calculadora = calculadora;
        this.rankings = rankings;
        this.limites = limites;
        this.fichas = fichas;
        this.reloj = reloj;
    }

    /** Requiere sesión iniciada: un visitante es redirigido al login (HU-08). */
    @GetMapping("/eventos/{id}/apostar")
    public String formulario(@PathVariable Long id, @RequestParam(required = false) Resultado resultado,
                             @RequestParam(required = false) Especial especial, Model model) {
        ApuestaForm form = new ApuestaForm();
        form.setResultado(resultado);
        form.setEspecial(especial);
        model.addAttribute("apuesta", form);
        return vista(catalogo.eventoDisponible(id), model);
    }

    @PostMapping("/eventos/{id}/apostar")
    public String apostar(@PathVariable Long id, @Valid @ModelAttribute("apuesta") ApuestaForm form,
                          BindingResult errores, Principal principal, Model model,
                          RedirectAttributes redireccion) {
        Evento evento = catalogo.eventoDisponible(id);
        if (!errores.hasErrors()) {
            try {
                Apuesta apuesta = apuestas.apostar(principal.getName(), List.of(
                        new SeleccionPedida(id, form.getResultado(), form.getEspecial(), form.getCuotaVista())),
                        form.getImporte());
                redireccion.addFlashAttribute("mensaje", "Apuesta registrada. Si aciertas cobras "
                        + apuesta.getGananciaPotencial().toPlainString().replace('.', ',') + " monedas.");
                return "redirect:/apuestas";
            } catch (SaldoInsuficienteException e) {
                errores.rejectValue("importe", "saldo", "No tienes saldo suficiente para esta apuesta");
            } catch (ImporteFueraDeLimitesException | JuegoResponsableException e) {
                errores.rejectValue("importe", "limites", e.getMessage());
            } catch (ResultadoNoValidoException e) {
                errores.rejectValue("opcion", "invalido", e.getMessage());
            } catch (CuotasCambiadasException e) {
                // HU-29: la cuota ha cambiado desde que la vio; se le enseña la nueva y decide
                form.setCuotaVista(null);
                errores.rejectValue("opcion", "cuotaCambiada",
                        "La cuota ha cambiado mientras decidías. Revisa la nueva y confirma otra vez.");
            } catch (IllegalArgumentException e) {
                errores.rejectValue("opcion", "invalido", e.getMessage());
            }
        }
        return vista(evento, model);
    }

    @GetMapping("/apuestas")
    public String misApuestas(Principal principal, Model model) {
        List<Apuesta> activas = apuestas.activas(principal.getName());
        BigDecimal comprometido = activas.stream().map(Apuesta::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("activas", activas);
        model.addAttribute("comprometido", comprometido);
        model.addAttribute("ahora", LocalDateTime.now(reloj));
        model.addAttribute("historial", apuestas.historial(principal.getName()));
        model.addAttribute("estadisticas", apuestas.estadisticas(principal.getName()));
        return "apuestas";
    }

    /** Ranking público: top 20 y, si no estás entre ellos, también tu posición (HU-36). */
    @GetMapping("/ranking")
    public String ranking(@RequestParam(defaultValue = "SALDO") CriterioRanking orden, Principal principal,
                          Model model) {
        List<PuestoRanking> todos = rankings.ranking(principal == null ? null : principal.getName(), orden);
        model.addAttribute("primeros", todos.stream().limit(20).toList());
        model.addAttribute("mio", todos.stream().filter(PuestoRanking::soyYo)
                .filter(p -> p.posicion() > 20).findFirst().orElse(null));
        model.addAttribute("total", todos.size());
        model.addAttribute("orden", orden);
        model.addAttribute("criterios", CriterioRanking.values());
        return "ranking";
    }

    /** Perfil público de un jugador desde el ranking (HU-48). */
    @GetMapping("/jugadores/{id}")
    public String jugador(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("perfil", rankings.perfil(id, principal == null ? null : principal.getName()));
        return "jugador";
    }

    @PostMapping("/apuestas/{id}/cancelar")
    public String cancelar(@PathVariable Long id, Principal principal, RedirectAttributes redireccion) {
        try {
            Apuesta apuesta = apuestas.cancelar(principal.getName(), id);
            redireccion.addFlashAttribute("mensaje", "Apuesta cancelada. Se te han devuelto "
                    + apuesta.getImporte().toPlainString().replace('.', ',') + " monedas.");
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", "Esa apuesta ya no se puede cancelar: el evento ha empezado.");
        }
        return "redirect:/apuestas";
    }

    /** Cambiar el importe de una apuesta activa (HU-27). */
    @PostMapping("/apuestas/{id}/importe")
    public String modificarImporte(@PathVariable Long id, @RequestParam(required = false) BigDecimal importe,
                                   Principal principal, RedirectAttributes redireccion) {
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            redireccion.addFlashAttribute("error", "Introduce un importe válido, con 2 decimales como mucho");
            return "redirect:/apuestas";
        }
        try {
            Apuesta apuesta = apuestas.modificarImporte(principal.getName(), id, importe);
            redireccion.addFlashAttribute("mensaje", "Importe cambiado a "
                    + apuesta.getImporte().toPlainString().replace('.', ',') + " monedas con cuota "
                    + apuesta.getCuota().toPlainString().replace('.', ',') + ". Si aciertas cobras "
                    + apuesta.getGananciaPotencial().toPlainString().replace('.', ',') + " monedas.");
        } catch (SaldoInsuficienteException e) {
            redireccion.addFlashAttribute("error", "No tienes saldo suficiente para subir el importe");
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", "Esa apuesta ya no se puede modificar: el evento ha empezado.");
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/apuestas";
    }

    private String vista(Evento evento, Model model) {
        model.addAttribute("evento", evento);
        model.addAttribute("cuotas", calculadora.calcular(evento));
        model.addAttribute("especiales", calculadora.especiales(evento));
        model.addAttribute("limitesApuesta", limites.actuales());
        model.addAttribute("caraACara", fichas.caraACara(evento));
        return "apostar";
    }
}

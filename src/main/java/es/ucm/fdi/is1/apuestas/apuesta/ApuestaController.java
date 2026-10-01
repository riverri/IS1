package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.security.Principal;
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
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.CatalogoService;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;
import jakarta.validation.Valid;

@Controller
public class ApuestaController {

    private final ApuestaService apuestas;
    private final CatalogoService catalogo;
    private final CalculadoraCuotas calculadora;

    public ApuestaController(ApuestaService apuestas, CatalogoService catalogo, CalculadoraCuotas calculadora) {
        this.apuestas = apuestas;
        this.catalogo = catalogo;
        this.calculadora = calculadora;
    }

    /** Requiere sesión iniciada: un visitante es redirigido al login (HU-08). */
    @GetMapping("/eventos/{id}/apostar")
    public String formulario(@PathVariable Long id, @RequestParam(required = false) Resultado resultado,
                             Model model) {
        ApuestaForm form = new ApuestaForm();
        form.setResultado(resultado);
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
                Apuesta apuesta = apuestas.apostar(principal.getName(), id, form.getResultado(), form.getImporte());
                redireccion.addFlashAttribute("mensaje", "Apuesta registrada. Si aciertas cobras "
                        + apuesta.getGananciaPotencial().toPlainString().replace('.', ',') + " monedas.");
                return "redirect:/apuestas";
            } catch (SaldoInsuficienteException e) {
                errores.rejectValue("importe", "saldo", "No tienes saldo suficiente para esta apuesta");
            } catch (ResultadoNoValidoException e) {
                errores.rejectValue("resultado", "invalido", e.getMessage());
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
        return "apuestas";
    }

    private String vista(Evento evento, Model model) {
        model.addAttribute("evento", evento);
        model.addAttribute("cuotas", calculadora.calcular(evento));
        return "apostar";
    }
}

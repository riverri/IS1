package es.ucm.fdi.is1.apuestas.quiniela;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;

/**
 * Páginas de la quiniela para los jugadores (HU-79 y HU-80). Ver las quinielas es público; echar un boleto exige
 * sesión ({@code SeguridadConfig}). La lógica está en {@link QuinielaService}.
 */
@Controller
@RequestMapping("/quinielas")
public class QuinielaController {

    private final QuinielaService quinielas;

    public QuinielaController(QuinielaService quinielas) {
        this.quinielas = quinielas;
    }

    /** GET /quinielas: las que admiten boletos y las demás. */
    @GetMapping
    public String lista(@RequestParam(defaultValue = "0") int pagina, Model model) {
        QuinielaService.Pagina resultado = quinielas.pagina(pagina);
        List<QuinielaService.Resumen> todas = resultado.resumenes();
        model.addAttribute("abiertas", todas.stream().filter(QuinielaService.Resumen::admiteBoletos).toList());
        model.addAttribute("otras", todas.stream().filter(r -> !r.admiteBoletos()
                && r.quiniela().getEstado() != EstadoQuiniela.ANULADA).toList());
        model.addAttribute("pagina", resultado);
        return "quinielas";
    }

    /** GET /quinielas/{id}: partidos, bote, mi boleto y, si se ha repartido, los aciertos de cada uno. */
    @GetMapping("/{id}")
    public String quiniela(@PathVariable Long id, Principal principal, Model model) {
        QuinielaService.Resumen resumen = quinielas.resumen(id);
        model.addAttribute("resumen", resumen);
        model.addAttribute("quiniela", resumen.quiniela());
        model.addAttribute("resultados", new Resultado[] {Resultado.LOCAL, Resultado.EMPATE, Resultado.VISITANTE});
        if (principal != null) {
            model.addAttribute("miBoleto", quinielas.boletoDe(principal.getName(), id).orElse(null));
        }
        if (resumen.quiniela().getEstado() == EstadoQuiniela.REPARTIDA) {
            model.addAttribute("boletos", quinielas.boletos(id));
        }
        return "quiniela";
    }

    /** POST /quinielas/{id}/boleto: un campo p0, p1... por partido con LOCAL, EMPATE o VISITANTE (CA-79.1). */
    @PostMapping("/{id}/boleto")
    public String echar(@PathVariable Long id, @RequestParam Map<String, String> campos, Principal principal,
                        RedirectAttributes redireccion) {
        try {
            int partidos = quinielas.quiniela(id).getPartidos().size();
            List<Resultado> pronosticos = new ArrayList<>();
            for (int i = 0; i < partidos; i++) {
                String valor = campos.get("p" + i);
                pronosticos.add(valor == null ? null : Resultado.valueOf(valor));
            }
            quinielas.echarBoleto(principal.getName(), id, pronosticos);
            redireccion.addFlashAttribute("mensaje", "Boleto echado. ¡Suerte!");
        } catch (SaldoInsuficienteException e) {
            redireccion.addFlashAttribute("error", "No tienes saldo suficiente para el boleto");
        } catch (IllegalArgumentException | IllegalStateException e) {
            // También el juego responsable (JuegoResponsableException es una IllegalArgumentException) y
            // Resultado.valueOf, si llega un valor que no es LOCAL, EMPATE ni VISITANTE
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/quinielas/" + id;
    }
}

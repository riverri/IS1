package es.ucm.fdi.is1.apuestas.casino;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.function.Supplier;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;

/**
 * Páginas del casino (HU-56 a HU-58). Todas cuelgan de {@code /casino} y exigen haber iniciado sesión (ver
 * SeguridadConfig: no están entre las públicas). La lógica está en {@link CasinoService}; aquí se traducen sus
 * errores a mensajes y se redirige, como en los controladores de las apuestas.
 */
@Controller
@RequestMapping("/casino")
public class CasinoController {

    private final CasinoService casino;

    public CasinoController(CasinoService casino) {
        this.casino = casino;
    }

    /** GET /casino: los juegos, los límites, la ventaja de la casa y la mano de blackjack a medias, si la hay. */
    @GetMapping
    public String inicio(Principal principal, Model model) {
        model.addAttribute("configuracion", casino.configuracion());
        model.addAttribute("manoEnCurso", casino.blackjackEnCurso(principal.getName()).orElse(null));
        model.addAttribute("ultimas", casino.historial(principal.getName()).stream().limit(5).toList());
        return "casino/inicio";
    }

    /** GET /casino/ruleta: el tapete con las apuestas posibles y el resultado de la última tirada. */
    @GetMapping("/ruleta")
    public String ruleta(Model model) {
        model.addAttribute("configuracion", casino.configuracion());
        model.addAttribute("apuestas", ApuestaRuleta.values());
        return "casino/ruleta";
    }

    /** POST /casino/ruleta: una tirada (CA-56.1 a CA-56.3); el resultado vuelve como mensaje. */
    @PostMapping("/ruleta")
    public String girar(@RequestParam(required = false) ApuestaRuleta tipo,
                        @RequestParam(required = false) Integer numero,
                        @RequestParam(required = false) BigDecimal importe, Principal principal,
                        RedirectAttributes redireccion) {
        Partida partida = intentar(() -> casino.girarRuleta(principal.getName(), tipo, numero, importe), redireccion);
        if (partida != null) {
            redireccion.addFlashAttribute("tirada", new Tirada(partida.getApuesta(), partida.getResultado(),
                    partida.getApostado(), partida.getPremio()));
        }
        // Se recuerdan la apuesta y el importe para volver a tirar con un clic
        redireccion.addFlashAttribute("tipoElegido", tipo);
        redireccion.addFlashAttribute("numeroElegido", numero);
        redireccion.addFlashAttribute("importeElegido", importe);
        return "redirect:/casino/ruleta";
    }

    /**
     * GET /casino/blackjack: la mesa. Con {@code mano} enseña esa mano (en curso o recién terminada); sin ella, la que
     * esté a medias o el formulario para empezar.
     */
    @GetMapping("/blackjack")
    public String blackjack(@RequestParam(required = false) Long mano, Principal principal, Model model) {
        ConfiguracionCasino configuracion = casino.configuracion();
        model.addAttribute("configuracion", configuracion);
        (mano != null ? casino.manoDelUsuario(principal.getName(), mano) : casino.blackjackEnCurso(principal.getName()))
                .ifPresent(p -> model.addAttribute("mesa", casino.mesa(p)));
        return "casino/blackjack";
    }

    /** POST /casino/blackjack: empieza una mano; si ya hay una a medias, se sigue con ella (CA-57.5). */
    @PostMapping("/blackjack")
    public String empezar(@RequestParam(required = false) BigDecimal importe, Principal principal,
                          RedirectAttributes redireccion) {
        Partida partida = intentar(() -> casino.empezarBlackjack(principal.getName(), importe), redireccion);
        return partida == null ? "redirect:/casino/blackjack" : "redirect:/casino/blackjack?mano=" + partida.getId();
    }

    /** POST /casino/blackjack/{id}/pedir: una carta más. */
    @PostMapping("/blackjack/{id}/pedir")
    public String pedir(@PathVariable Long id, Principal principal, RedirectAttributes redireccion) {
        intentar(() -> casino.pedir(principal.getName(), id), redireccion);
        return "redirect:/casino/blackjack?mano=" + id;
    }

    /** POST /casino/blackjack/{id}/plantarse: juega la banca. */
    @PostMapping("/blackjack/{id}/plantarse")
    public String plantarse(@PathVariable Long id, Principal principal, RedirectAttributes redireccion) {
        intentar(() -> casino.plantarse(principal.getName(), id), redireccion);
        return "redirect:/casino/blackjack?mano=" + id;
    }

    /** POST /casino/blackjack/{id}/doblar: dobla la apuesta, una carta y termina la mano. */
    @PostMapping("/blackjack/{id}/doblar")
    public String doblar(@PathVariable Long id, Principal principal, RedirectAttributes redireccion) {
        intentar(() -> casino.doblar(principal.getName(), id), redireccion);
        return "redirect:/casino/blackjack?mano=" + id;
    }

    /** GET /casino/historial: las últimas partidas (CA-58.1). */
    @GetMapping("/historial")
    public String historial(Principal principal, Model model) {
        model.addAttribute("partidas", casino.historial(principal.getName()));
        return "casino/historial";
    }

    /**
     * Resultado de una tirada para enseñarlo tras redirigir. Los atributos flash se guardan un momento en la sesión,
     * por eso es un record pequeño y serializable y no la entidad {@link Partida}.
     */
    public record Tirada(String apuesta, String resultado, BigDecimal apostado, BigDecimal premio)
            implements Serializable {

        public boolean isGanada() {
            return premio.signum() > 0;
        }
    }

    /**
     * Ejecuta la jugada y, si una regla la impide, deja el motivo como mensaje de error y devuelve null.
     * {@code Supplier}: una lambda sin parámetros que devuelve algo (aquí, la partida).
     */
    private static Partida intentar(Supplier<Partida> jugada, RedirectAttributes redireccion) {
        try {
            return jugada.get();
        } catch (SaldoInsuficienteException e) {
            redireccion.addFlashAttribute("error", "No tienes saldo suficiente para esta apuesta");
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Límites del casino, juego responsable (HU-10), casino cerrado, mano terminada…
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return null;
    }
}

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

/**
 * Controlador web de las apuestas del jugador: apuesta simple desde la página del partido (HU-23, HU-52),
 * Mis apuestas (HU-24, HU-34, HU-35), cancelar (HU-26) y cambiar el importe (HU-27), además del ranking
 * (HU-36) y el perfil público de un jugador (HU-48). Delega en {@link ApuestaService} y {@link RankingService}
 * y convierte sus excepciones en mensajes para el usuario.
 * (Qué son {@code @GetMapping}, {@code Model}, {@code Principal}... está explicado en
 * {@link es.ucm.fdi.is1.apuestas.usuarios.UsuariosController}.)
 */
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

    /**
     * GET /eventos/{id}/apostar: página del partido con sus cuotas y el formulario de apuesta simple.
     * Puede llegar con el pronóstico ya marcado ({@code resultado} o {@code especial}).
     * Requiere sesión iniciada: un visitante es redirigido al login (HU-08).
     * Si el evento ya no admite apuestas, 404 ({@link es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException}).
     */
    @GetMapping("/eventos/{id}/apostar")
    // @PathVariable: el {id} de la dirección (/eventos/7/apostar → id = 7).
    // @RequestParam(required = false): parámetro opcional de la URL (?resultado=LOCAL); si no viene, llega null
    public String formulario(@PathVariable Long id, @RequestParam(required = false) Resultado resultado,
                             @RequestParam(required = false) Especial especial, Model model) {
        ApuestaForm form = new ApuestaForm();
        form.setResultado(resultado);
        form.setEspecial(especial);
        model.addAttribute("apuesta", form);
        return vista(catalogo.eventoDisponible(id), model);
    }

    /**
     * POST /eventos/{id}/apostar: registra la apuesta simple. Si sale bien, lleva a Mis apuestas con la
     * ganancia potencial; si no, vuelve a la página del partido con el error junto al campo afectado.
     */
    @PostMapping("/eventos/{id}/apostar")
    public String apostar(@PathVariable Long id, @Valid @ModelAttribute("apuesta") ApuestaForm form,
                          BindingResult errores, Principal principal, Model model,
                          RedirectAttributes redireccion) {
        Evento evento = catalogo.eventoDisponible(id);
        // Primero las validaciones del formulario (@Valid); solo si pasan se intenta apostar
        if (!errores.hasErrors()) {
            try {
                Apuesta apuesta = apuestas.apostar(principal.getName(), List.of(
                        new SeleccionPedida(id, form.getResultado(), form.getEspecial(), form.getCuotaVista())),
                        form.getImporte());
                redireccion.addFlashAttribute("mensaje", "Apuesta registrada. Si aciertas cobras "
                        + apuesta.getGananciaPotencial().toPlainString().replace('.', ',') + " monedas.");
                return "redirect:/apuestas";
            } catch (SaldoInsuficienteException e) {
                // Cada catch recoge un tipo de error y lo enseña junto al campo que corresponde.
                // "A | B" en un catch recoge cualquiera de las dos excepciones
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
                // El resto de reglas (cuota total, combinar, creador…); va la última porque
                // ImporteFueraDeLimitesException y JuegoResponsableException son subclases suyas
                errores.rejectValue("opcion", "invalido", e.getMessage());
            }
        }
        return vista(evento, model);
    }

    /**
     * GET /apuestas: Mis apuestas. Las activas con el total comprometido (HU-24), el historial (HU-34)
     * y las estadísticas (HU-35).
     */
    @GetMapping("/apuestas")
    public String misApuestas(Principal principal, Model model) {
        List<Apuesta> activas = apuestas.activas(principal.getName());
        // Total comprometido: suma de los importes de las apuestas activas
        BigDecimal comprometido = activas.stream().map(Apuesta::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("activas", activas);
        model.addAttribute("comprometido", comprometido);
        // La vista llama a cancelable(ahora) para mostrar o no los botones de cancelar y cambiar importe
        model.addAttribute("ahora", LocalDateTime.now(reloj));
        model.addAttribute("historial", apuestas.historial(principal.getName()));
        model.addAttribute("estadisticas", apuestas.estadisticas(principal.getName()));
        return "apuestas";
    }

    /**
     * GET /ranking?orden=...: ranking público, por saldo si no se indica criterio. Muestra el top 20 y,
     * si no estás entre ellos, también tu posición (HU-36, CA-36.4). Sin sesión no hay "tu posición".
     */
    @GetMapping("/ranking")
    public String ranking(@RequestParam(defaultValue = "SALDO") CriterioRanking orden, Principal principal,
                          Model model) {
        List<PuestoRanking> todos = rankings.ranking(principal == null ? null : principal.getName(), orden);
        model.addAttribute("primeros", todos.stream().limit(20).toList());
        // Tu fila aparte solo si no sale ya en el top 20
        model.addAttribute("mio", todos.stream().filter(PuestoRanking::soyYo)
                .filter(p -> p.posicion() > 20).findFirst().orElse(null));
        model.addAttribute("total", todos.size());
        model.addAttribute("orden", orden);
        model.addAttribute("criterios", CriterioRanking.values());
        return "ranking";
    }

    /**
     * GET /jugadores/{id}: perfil público de un jugador desde el ranking (HU-48), con sus estadísticas
     * pero sin sus apuestas. 404 si no es un jugador del ranking ({@link JugadorNoEncontradoException}).
     */
    @GetMapping("/jugadores/{id}")
    public String jugador(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("perfil", rankings.perfil(id, principal == null ? null : principal.getName()));
        return "jugador";
    }

    /**
     * POST /apuestas/{id}/cancelar: cancela la apuesta y devuelve el importe (HU-26). Vuelve a Mis apuestas
     * con un mensaje. Si la apuesta no es del usuario, 404 ({@link ApuestaNoEncontradaException}).
     */
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

    /**
     * POST /apuestas/{id}/importe: cambia el importe de una apuesta activa (HU-27) y vuelve a Mis apuestas
     * con la nueva cuota y la nueva ganancia potencial, o con el error.
     */
    @PostMapping("/apuestas/{id}/importe")
    public String modificarImporte(@PathVariable Long id, @RequestParam(required = false) BigDecimal importe,
                                   Principal principal, RedirectAttributes redireccion) {
        // Las mismas reglas que @DecimalMin y @Digits de ApuestaForm, a mano porque aquí no hay formulario.
        // stripTrailingZeros().scale() = número de decimales sin contar los ceros finales (5,10 → 1 decimal)
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

    /**
     * Rellena la página "apostar": cuotas 1X2, otros tipos de apuesta (solo en fútbol, HU-52),
     * límites de importe y cara a cara de los dos equipos (HU-33).
     */
    private String vista(Evento evento, Model model) {
        model.addAttribute("evento", evento);
        model.addAttribute("cuotas", calculadora.calcular(evento));
        model.addAttribute("especiales", calculadora.especiales(evento));
        model.addAttribute("limitesApuesta", limites.actuales());
        model.addAttribute("caraACara", fichas.caraACara(evento));
        return "apostar";
    }
}

package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
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
import es.ucm.fdi.is1.apuestas.cuotas.RegistroCuotas;
import es.ucm.fdi.is1.apuestas.seguimiento.AlertasService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.CatalogoService;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.eventos.FichaEquipoService;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;
import es.ucm.fdi.is1.apuestas.web.Monedas;
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
    private final RegistroCuotas registroCuotas;
    private final AlertasService alertas;
    private final SupercuotaService supercuotas;
    private final LogrosService logros;
    private final Clock reloj;

    public ApuestaController(ApuestaService apuestas, CatalogoService catalogo, CalculadoraCuotas calculadora,
                             RankingService rankings, LimitesService limites, FichaEquipoService fichas,
                             RegistroCuotas registroCuotas, AlertasService alertas, SupercuotaService supercuotas,
                             LogrosService logros, Clock reloj) {
        this.apuestas = apuestas;
        this.catalogo = catalogo;
        this.calculadora = calculadora;
        this.rankings = rankings;
        this.limites = limites;
        this.fichas = fichas;
        this.registroCuotas = registroCuotas;
        this.alertas = alertas;
        this.supercuotas = supercuotas;
        this.logros = logros;
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
                             @RequestParam(required = false) Especial especial, Principal principal, Model model) {
        ApuestaForm form = new ApuestaForm();
        form.setResultado(resultado);
        form.setEspecial(especial);
        model.addAttribute("apuesta", form);
        return vista(catalogo.eventoDisponible(id), principal, model);
    }

    /**
     * POST /eventos/{id}/apostar: registra la apuesta simple. Si sale bien, lleva a Mis apuestas con la
     * ganancia potencial; si no, vuelve a la página del partido con el error junto al campo afectado.
     */
    @PostMapping("/eventos/{id}/apostar")
    public String apostar(@PathVariable Long id, @Valid @ModelAttribute("apuesta") ApuestaForm form,
                          BindingResult errores, Principal principal, Model model,
                          RedirectAttributes redireccion) {
        Evento evento;
        try {
            evento = catalogo.eventoDisponible(id);
        } catch (EventoNoDisponibleException e) {
            // Ha empezado o se ha suspendido mientras rellenaba el formulario: un aviso en vez de un 404
            return yaNoAdmiteApuestas(redireccion);
        }
        // Primero las validaciones del formulario (@Valid); solo si pasan se intenta apostar
        if (!errores.hasErrors()) {
            try {
                Apuesta apuesta = apuestas.apostar(principal.getName(), List.of(
                        new SeleccionPedida(id, form.getResultado(), form.getEspecial(), form.getCuotaVista())),
                        form.getImporte());
                redireccion.addFlashAttribute("mensaje", "Apuesta registrada. Si aciertas cobras "
                        + Monedas.texto(apuesta.getGananciaPotencial()) + " monedas.");
                return "redirect:/apuestas";
            } catch (EventoNoDisponibleException e) {
                return yaNoAdmiteApuestas(redireccion);
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
        return vista(evento, principal, model);
    }

    /** El partido ya no admite apuestas (ha empezado, se ha suspendido…): al catálogo con el aviso, sin apostar. */
    static String yaNoAdmiteApuestas(RedirectAttributes redireccion) {
        redireccion.addFlashAttribute("errorBoleto", "Ese partido ya no admite apuestas: no se ha apostado nada.");
        return "redirect:/eventos";
    }

    /**
     * GET /apuestas: Mis apuestas. Las activas con el total comprometido (HU-24), el historial (HU-34)
     * y las estadísticas (HU-35).
     */
    @GetMapping("/apuestas")
    public String misApuestas(@RequestParam(defaultValue = "0") int paginaActivas,
                              @RequestParam(defaultValue = "0") int paginaHistorial,
                              Principal principal, Model model) {
        var activasPaginadas = apuestas.paginaActivas(principal.getName(), paginaActivas);
        List<Apuesta> activas = activasPaginadas.apuestas();
        BigDecimal comprometido = apuestas.importeActivo(principal.getName());
        model.addAttribute("activas", activas);
        model.addAttribute("comprometido", comprometido);
        model.addAttribute("activasTotales", apuestas.cuentaActivas(principal.getName()));
        model.addAttribute("paginaActivas", activasPaginadas);
        // La vista llama a cancelable(ahora) para mostrar o no los botones de cancelar y cambiar importe
        model.addAttribute("ahora", LocalDateTime.now(reloj));
        model.addAttribute("ofertasCashOut", apuestas.ofertasCashOut(activas, LocalDateTime.now(reloj)));
        var historialPaginado = apuestas.paginaHistorial(principal.getName(), paginaHistorial);
        List<Apuesta> historial = historialPaginado.apuestas();
        model.addAttribute("historial", historial);
        model.addAttribute("paginaHistorial", historialPaginado);
        model.addAttribute("estadisticas", apuestas.estadisticas(principal.getName()));
        model.addAttribute("porCompeticion", apuestas.estadisticasPorCompeticion(principal.getName()));
        return "apuestas";
    }

    /**
     * GET /ranking?orden=...&amp;juego=...: ranking público, por saldo si no se indica criterio. Muestra el top 20 y,
     * si no estás entre ellos, también tu posición (HU-36, CA-36.4). Sin sesión no hay "tu posición". Con
     * {@code juego}, solo los que han jugado a ese tipo de juego y con sus estadísticas de ese tipo (HU-84).
     */
    @GetMapping("/ranking")
    public String ranking(@RequestParam(defaultValue = "SALDO") CriterioRanking orden,
                          @RequestParam(required = false) TipoJuego juego, Principal principal, Model model) {
        // HU-84: en un tipo de juego no se ordena por saldo, ni por aciertos en cripto (CA-84.2)
        if (juego != null && !juego.admite(orden)) {
            orden = CriterioRanking.GANANCIAS;
        }
        List<PuestoRanking> todos = rankings.ranking(principal == null ? null : principal.getName(), orden, null, juego);
        model.addAttribute("primeros", todos.stream().limit(20).toList());
        // Tu fila aparte solo si no sale ya en el top 20
        model.addAttribute("mio", todos.stream().filter(PuestoRanking::soyYo)
                .filter(p -> p.posicion() > 20).findFirst().orElse(null));
        model.addAttribute("total", todos.size());
        model.addAttribute("orden", orden);
        model.addAttribute("juego", juego);
        model.addAttribute("juegos", TipoJuego.values());
        model.addAttribute("criterios", juego == null ? List.of(CriterioRanking.values())
                : Arrays.stream(CriterioRanking.values()).filter(juego::admite).toList());
        return "ranking";
    }

    /**
     * GET /jugadores/{id}: perfil público de un jugador desde el ranking (HU-48), con sus estadísticas
     * pero sin sus apuestas. 404 si no es un jugador del ranking ({@link JugadorNoEncontradoException}).
     */
    @GetMapping("/jugadores/{id}")
    public String jugador(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("perfil", rankings.perfil(id, principal == null ? null : principal.getName()));
        // HU-88: sus insignias
        model.addAttribute("logros", logros.de(id));
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
                    + Monedas.texto(apuesta.getImporte()) + " monedas.");
        } catch (IllegalStateException e) {
            // El mensaje lo da Apuesta.motivoNoCancelable: partido empezado, mercado cerrado o apuesta no activa
            redireccion.addFlashAttribute("error", "Esa apuesta ya no se puede cancelar: " + e.getMessage() + ".");
        }
        return "redirect:/apuestas";
    }

    /** POST /apuestas/{id}/cash-out: confirma la oferta de la combinada tras validar de nuevo cuotas y eventos. */
    @PostMapping("/apuestas/{id}/cash-out")
    public String cashOut(@PathVariable Long id, @RequestParam(required = false) BigDecimal ofertaVista,
                          Principal principal, RedirectAttributes redireccion) {
        try {
            Apuesta apuesta = apuestas.cobrarAnticipado(principal.getName(), id, ofertaVista);
            redireccion.addFlashAttribute("mensaje", (apuesta.isCombinada() ? "Combinada cerrada" : "Apuesta cerrada")
                    + ". Se han abonado " + Monedas.texto(apuesta.getPagado()) + " monedas.");
        } catch (OfertaCashOutCambiadaException e) {
            redireccion.addFlashAttribute("aviso", e.getMessage() + " Nueva oferta: "
                    + Monedas.texto(e.getOfertaNueva()) + " monedas.");
            redireccion.addFlashAttribute("cashOutNuevaId", id);
            redireccion.addFlashAttribute("cashOutNuevaOferta", e.getOfertaNueva());
        } catch (ApuestaNoEncontradaException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", "No se pudo completar el cash-out: " + e.getMessage());
        }
        return "redirect:/apuestas";
    }

    /**
     * POST /apuestas/{id}/importe: cambia el importe de una apuesta activa (HU-27) y vuelve a Mis apuestas
     * con la nueva cuota y la nueva ganancia potencial, o con el error.
     * {@code cuotaVista} es la cuota total que el usuario tenía en pantalla: si la actual es otra, no se cambia
     * nada y se le enseña la nueva; al volver a guardar, el formulario ya lleva la nueva (HU-29).
     */
    @PostMapping("/apuestas/{id}/importe")
    public String modificarImporte(@PathVariable Long id, @RequestParam(required = false) BigDecimal importe,
                                   @RequestParam(required = false) BigDecimal cuotaVista,
                                   Principal principal, RedirectAttributes redireccion) {
        // Las mismas reglas que @DecimalMin y @Digits de ApuestaForm, a mano porque aquí no hay formulario.
        // stripTrailingZeros().scale() = número de decimales sin contar los ceros finales (5,10 → 1 decimal)
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            redireccion.addFlashAttribute("error", "Introduce un importe válido, con 2 decimales como mucho");
            return "redirect:/apuestas";
        }
        try {
            Apuesta apuesta = apuestas.modificarImporte(principal.getName(), id, importe, cuotaVista);
            redireccion.addFlashAttribute("mensaje", "Importe cambiado a "
                    + Monedas.texto(apuesta.getImporte()) + " monedas con cuota "
                    + apuesta.getCuota().toPlainString().replace('.', ',') + ". Si aciertas cobras "
                    + Monedas.texto(apuesta.getGananciaPotencial()) + " monedas.");
        } catch (CuotaApuestaCambiadaException e) {
            // Se le enseña la cuota nueva; la plantilla la pone en el formulario de esa apuesta (y lo deja abierto)
            redireccion.addFlashAttribute("aviso", "La cuota de tu apuesta ha cambiado: ahora es "
                    + e.getCuotaNueva().toPlainString().replace('.', ',')
                    + ". Si te parece bien, vuelve a guardar el importe.");
            redireccion.addFlashAttribute("cuotaNuevaId", id);
            redireccion.addFlashAttribute("cuotaNueva", e.getCuotaNueva());
            redireccion.addFlashAttribute("importeNuevo", importe);
        } catch (SaldoInsuficienteException e) {
            redireccion.addFlashAttribute("error", "No tienes saldo suficiente para subir el importe");
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", "Esa apuesta ya no se puede modificar: " + e.getMessage() + ".");
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/apuestas";
    }

    /**
     * Rellena la página "apostar": cuotas 1X2, otros tipos de apuesta (solo en fútbol, HU-52),
     * límites de importe y cara a cara de los dos equipos (HU-33).
     */
    private String vista(Evento evento, Principal principal, Model model) {
        // HU-91: se apuntan las cuotas por si han cambiado sin apostar (forma de los equipos, la API)
        registroCuotas.registrar(evento);
        model.addAttribute("evolucion", registroCuotas.evolucion(evento));
        // HU-90: mis alertas de cuota en este partido
        model.addAttribute("misAlertas", principal == null ? List.of() : alertas.mias(principal.getName(), evento));
        model.addAttribute("evento", evento);
        // HU-87: con supercuota, su resultado se enseña ya mejorado
        model.addAttribute("cuotas", supercuotas.cuotasConSupercuota(evento));
        model.addAttribute("supercuota", supercuotas.vigente(evento).orElse(null));
        model.addAttribute("especiales", calculadora.especiales(evento));
        model.addAttribute("limitesApuesta", limites.actuales());
        model.addAttribute("caraACara", fichas.caraACara(evento));
        return "apostar";
    }
}

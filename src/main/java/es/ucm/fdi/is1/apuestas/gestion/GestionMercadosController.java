package es.ucm.fdi.is1.apuestas.gestion;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.apuesta.ResolucionMercadosService;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;
import es.ucm.fdi.is1.apuestas.mercados.MercadoService;
import jakarta.validation.Valid;

/**
 * Controlador de los mercados a largo plazo en el panel del creador: alta, cuotas, cierre, ganador y
 * anulación (HU-45). Sus rutas cuelgan de {@code /gestion/mercados}, así que solo las usa el rol CREADOR
 * (ver SeguridadConfig). La parte pública, donde los usuarios apuestan (HU-44), está en
 * {@code MercadosController}.
 * <p>
 * Las acciones vuelven siempre a la ficha del mercado con un mensaje de éxito o con el error.
 */
@Controller
@RequestMapping("/gestion/mercados")
public class GestionMercadosController {

    private final MercadoService mercados;
    private final ResolucionMercadosService resolucion;
    private final Clock reloj;

    public GestionMercadosController(MercadoService mercados, ResolucionMercadosService resolucion, Clock reloj) {
        this.mercados = mercados;
        this.resolucion = resolucion;
        this.reloj = reloj;
    }

    /** Deportes para el desplegable del formulario de alta. */
    @ModelAttribute("deportes")
    public Deporte[] deportes() {
        return Deporte.values();
    }

    /** GET /gestion/mercados/nuevo: formulario de alta de un mercado. */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("mercado", new MercadoForm());
        return "gestion/mercado";
    }

    /**
     * POST /gestion/mercados/nuevo: crea el mercado y lleva a su ficha (CA-45.1). Los errores al leer
     * los candidatos (formato, cuotas, menos de 2) se muestran junto al campo de candidatos.
     */
    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("mercado") MercadoForm form, BindingResult errores,
                        RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                Mercado mercado = mercados.crear(form.getNombre(), form.getDeporte(), form.getCierre(),
                        form.getCandidatos());
                redireccion.addFlashAttribute("mensaje", "Mercado creado: " + mercado.getNombre());
                return "redirect:/gestion/mercados/" + mercado.getId();
            } catch (IllegalArgumentException | IllegalStateException e) {
                errores.rejectValue("candidatos", "invalido", e.getMessage());
            }
        }
        return "gestion/mercado";
    }

    /**
     * GET /gestion/mercados/{id}: ficha del mercado para el creador, con lo apostado a cada candidato
     * y si todavía admite apuestas.
     */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Mercado mercado = mercados.mercado(id);
        model.addAttribute("mercado", mercado);
        model.addAttribute("volumen", resolucion.volumen(id));
        model.addAttribute("abierto", mercado.admiteApuestas(LocalDateTime.now(reloj)));
        return "gestion/mercado-detalle";
    }

    /** POST /gestion/mercados/{id}/candidatos: añade un candidato (solo con el mercado abierto). */
    @PostMapping("/{id}/candidatos")
    public String anadirCandidato(@PathVariable Long id, @RequestParam String nombre,
                                  @RequestParam(required = false) BigDecimal cuota, RedirectAttributes redireccion) {
        return accion(id, () -> mercados.anadirCandidato(id, nombre, cuota),
                "Candidato añadido: " + nombre.trim(), redireccion);
    }

    /**
     * POST /gestion/mercados/{id}/candidatos/{candidatoId}/cuota: cambia la cuota de un candidato.
     * La cuota es opcional en la petición para que, si falta, el error lo explique el propio candidato.
     */
    @PostMapping("/{id}/candidatos/{candidatoId}/cuota")
    public String cambiarCuota(@PathVariable Long id, @PathVariable Long candidatoId,
                               @RequestParam(required = false) BigDecimal cuota, RedirectAttributes redireccion) {
        return accion(id, () -> mercados.cambiarCuota(id, candidatoId, cuota),
                "Cuota actualizada. Las apuestas ya hechas mantienen la suya.", redireccion);
    }

    /** POST /gestion/mercados/{id}/cerrar: el mercado deja de admitir apuestas antes de su fecha (CA-45.2). */
    @PostMapping("/{id}/cerrar")
    public String cerrar(@PathVariable Long id, RedirectAttributes redireccion) {
        return accion(id, () -> mercados.cerrar(id), "Mercado cerrado: ya no admite apuestas.", redireccion);
    }

    /**
     * POST /gestion/mercados/{id}/ganador: marca (o corrige) el candidato ganador. Se cobran las apuestas
     * a ese candidato y las demás pasan a perdidas (CA-45.3).
     */
    @PostMapping("/{id}/ganador")
    public String ganador(@PathVariable Long id, @RequestParam Long candidato, RedirectAttributes redireccion) {
        try {
            int resueltas = resolucion.resolver(id, candidato);
            redireccion.addFlashAttribute("mensaje", "Ganador guardado. Apuestas resueltas: " + resueltas + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/mercados/" + id;
    }

    /** POST /gestion/mercados/{id}/anular: anula el mercado y devuelve el importe de sus apuestas. */
    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes redireccion) {
        return accion(id, () -> resolucion.anular(id),
                "Mercado anulado. Se ha devuelto el importe de todas sus apuestas.", redireccion);
    }

    /** Ejecuta la acción y vuelve a la ficha del mercado; si una regla la impide, muestra el motivo. */
    private String accion(Long id, Runnable accion, String mensaje, RedirectAttributes redireccion) {
        try {
            accion.run();
            redireccion.addFlashAttribute("mensaje", mensaje);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/mercados/" + id;
    }
}

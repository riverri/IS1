package es.ucm.fdi.is1.apuestas.gestion;

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

import es.ucm.fdi.is1.apuestas.api.ApiProperties;
import es.ucm.fdi.is1.apuestas.api.ResumenSincronizacion;
import es.ucm.fdi.is1.apuestas.api.SincronizacionService;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import jakarta.validation.Valid;

/** Panel del creador de apuestas (solo rol CREADOR, ver SeguridadConfig). */
@Controller
@RequestMapping("/gestion")
public class GestionController {

    private final GestionService gestion;
    private final ResolucionService resolucion;
    private final SincronizacionService sincronizacion;
    private final ApiProperties api;
    private final Clock reloj;

    public GestionController(GestionService gestion, ResolucionService resolucion,
                             SincronizacionService sincronizacion, ApiProperties api, Clock reloj) {
        this.gestion = gestion;
        this.resolucion = resolucion;
        this.sincronizacion = sincronizacion;
        this.api = api;
        this.reloj = reloj;
    }

    /** Descarga ahora mismo partidos y resultados de la API (HU-21). */
    @PostMapping("/sincronizar")
    public String sincronizar(RedirectAttributes redireccion) {
        try {
            ResumenSincronizacion resumen = sincronizacion.sincronizar();
            redireccion.addFlashAttribute("mensaje", "Sincronizado con la API: " + resumen + ".");
            if (!resumen.getErrores().isEmpty()) {
                redireccion.addFlashAttribute("error", String.join(" · ", resumen.getErrores()));
            }
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion";
    }

    @ModelAttribute("deportes")
    public Deporte[] deportes() {
        return Deporte.values();
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("competiciones", gestion.competiciones());
        model.addAttribute("equipos", gestion.equipos());
        model.addAttribute("eventos", gestion.eventos());
        model.addAttribute("ahora", LocalDateTime.now(reloj));
        model.addAttribute("apiConfigurada", api.configurada());
        return "gestion/panel";
    }

    // --- Competiciones ---

    @GetMapping("/competiciones/nueva")
    public String nuevaCompeticion(Model model) {
        model.addAttribute("competicion", new CompeticionForm());
        return "gestion/competicion";
    }

    @PostMapping("/competiciones/nueva")
    public String crearCompeticion(@Valid @ModelAttribute("competicion") CompeticionForm form,
                                   BindingResult errores, RedirectAttributes redireccion) {
        if (errores.hasErrors()) {
            return "gestion/competicion";
        }
        gestion.crearCompeticion(form);
        redireccion.addFlashAttribute("mensaje", "Competición creada: " + form.getNombre());
        return "redirect:/gestion";
    }

    // --- Equipos ---

    @GetMapping("/equipos/nuevo")
    public String nuevoEquipo(Model model) {
        model.addAttribute("equipo", new EquipoForm());
        model.addAttribute("competiciones", gestion.competiciones());
        return "gestion/equipo";
    }

    @PostMapping("/equipos/nuevo")
    public String crearEquipo(@Valid @ModelAttribute("equipo") EquipoForm form, BindingResult errores,
                              Model model, RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                gestion.crearEquipo(form);
                redireccion.addFlashAttribute("mensaje", "Equipo creado: " + form.getNombre());
                return "redirect:/gestion";
            } catch (DatosInvalidosException e) {
                errores.rejectValue(e.getCampo(), "invalido", e.getMessage());
            }
        }
        model.addAttribute("competiciones", gestion.competiciones());
        return "gestion/equipo";
    }

    @GetMapping("/equipos/{id}/editar")
    public String editarEquipo(@PathVariable Long id, Model model) {
        Equipo equipo = gestion.equipo(id);
        EdicionEquipoForm form = new EdicionEquipoForm();
        form.setCalidad(equipo.getCalidad());
        form.setEscudoUrl(equipo.getEscudoUrl());
        model.addAttribute("equipo", equipo);
        model.addAttribute("edicion", form);
        return "gestion/editar-equipo";
    }

    @PostMapping("/equipos/{id}/editar")
    public String guardarEquipo(@PathVariable Long id, @Valid @ModelAttribute("edicion") EdicionEquipoForm form,
                                BindingResult errores, Model model, RedirectAttributes redireccion) {
        Equipo equipo = gestion.equipo(id);
        if (errores.hasErrors()) {
            model.addAttribute("equipo", equipo);
            return "gestion/editar-equipo";
        }
        gestion.editarEquipo(id, form);
        redireccion.addFlashAttribute("mensaje", "Equipo actualizado: " + equipo.getNombre());
        return "redirect:/gestion";
    }

    // --- Eventos ---

    @GetMapping("/eventos/nuevo")
    public String nuevoEvento(Model model) {
        model.addAttribute("evento", new EventoForm());
        return formularioEvento(model);
    }

    @PostMapping("/eventos/nuevo")
    public String crearEvento(@Valid @ModelAttribute("evento") EventoForm form, BindingResult errores,
                              Model model, RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                gestion.crearEvento(form);
                redireccion.addFlashAttribute("mensaje", "Evento creado");
                return "redirect:/gestion";
            } catch (DatosInvalidosException e) {
                errores.rejectValue(e.getCampo(), "invalido", e.getMessage());
            }
        }
        return formularioEvento(model);
    }

    @GetMapping("/eventos/{id}")
    public String evento(@PathVariable Long id, Model model) {
        Evento evento = resolucion.evento(id);
        model.addAttribute("evento", evento);
        model.addAttribute("volumen", resolucion.volumen(id));
        model.addAttribute("empezado", evento.haEmpezado(LocalDateTime.now(reloj)));
        return "gestion/evento-detalle";
    }

    @PostMapping("/eventos/{id}/resultado")
    public String resultado(@PathVariable Long id, @RequestParam Resultado resultado,
                            RedirectAttributes redireccion) {
        try {
            int resueltas = resolucion.introducirResultado(id, resultado);
            redireccion.addFlashAttribute("mensaje",
                    "Resultado guardado: " + resultado.getDescripcion() + ". Apuestas resueltas: " + resueltas + ".");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/eventos/" + id;
    }

    @PostMapping("/eventos/{id}/suspender")
    public String suspender(@PathVariable Long id, RedirectAttributes redireccion) {
        return accion(id, () -> resolucion.suspender(id), "Evento suspendido: no admite apuestas nuevas.", redireccion);
    }

    @PostMapping("/eventos/{id}/reactivar")
    public String reactivar(@PathVariable Long id, RedirectAttributes redireccion) {
        return accion(id, () -> resolucion.reactivar(id), "Evento reactivado.", redireccion);
    }

    @PostMapping("/eventos/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes redireccion) {
        return accion(id, () -> resolucion.anular(id),
                "Evento anulado. Se ha devuelto el importe de todas sus apuestas.", redireccion);
    }

    private String accion(Long id, Runnable accion, String mensaje, RedirectAttributes redireccion) {
        try {
            accion.run();
            redireccion.addFlashAttribute("mensaje", mensaje);
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/eventos/" + id;
    }

    private String formularioEvento(Model model) {
        model.addAttribute("competiciones", gestion.competiciones());
        model.addAttribute("equipos", gestion.equipos());
        return "gestion/evento";
    }
}

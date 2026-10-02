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
import es.ucm.fdi.is1.apuestas.api.SincronizacionPlantillas;
import es.ucm.fdi.is1.apuestas.api.SincronizacionService;
import es.ucm.fdi.is1.apuestas.apuesta.Limites;
import es.ucm.fdi.is1.apuestas.apuesta.LimitesService;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.Forma;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.PlantillaService;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.mercados.MercadoService;
import jakarta.validation.Valid;

/** Panel del creador de apuestas (solo rol CREADOR, ver SeguridadConfig). */
@Controller
@RequestMapping("/gestion")
public class GestionController {

    private final GestionService gestion;
    private final ResolucionService resolucion;
    private final SincronizacionService sincronizacion;
    private final SincronizacionPlantillas sincronizacionPlantillas;
    private final MercadoService mercados;
    private final LimitesService limites;
    private final PlantillaService plantillas;
    private final ApiProperties api;
    private final Clock reloj;

    public GestionController(GestionService gestion, ResolucionService resolucion,
                             SincronizacionService sincronizacion, SincronizacionPlantillas sincronizacionPlantillas,
                             MercadoService mercados, LimitesService limites, PlantillaService plantillas,
                             ApiProperties api, Clock reloj) {
        this.gestion = gestion;
        this.resolucion = resolucion;
        this.sincronizacion = sincronizacion;
        this.sincronizacionPlantillas = sincronizacionPlantillas;
        this.plantillas = plantillas;
        this.mercados = mercados;
        this.limites = limites;
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

    /** Descarga ahora mismo las plantillas reales de los equipos. */
    @PostMapping("/sincronizar-plantillas")
    public String sincronizarPlantillas(RedirectAttributes redireccion) {
        try {
            SincronizacionPlantillas.Resumen resumen = sincronizacionPlantillas.sincronizar();
            redireccion.addFlashAttribute("mensaje", "Plantillas descargadas: " + resumen + ".");
            if (!resumen.errores().isEmpty()) {
                redireccion.addFlashAttribute("error", String.join(" · ", resumen.errores()));
            }
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion";
    }

    // --- Jugadores ---

    @PostMapping("/equipos/{id}/jugadores")
    public String anadirJugador(@PathVariable Long id, @RequestParam(required = false) String nombre,
                                @RequestParam(required = false) Posicion posicion,
                                @RequestParam(required = false) Integer dorsal,
                                @RequestParam(required = false) Double nota,
                                @RequestParam(required = false) String nacionalidad, RedirectAttributes redireccion) {
        try {
            Jugador jugador = plantillas.anadir(id, nombre, posicion, dorsal, nota, nacionalidad);
            redireccion.addFlashAttribute("mensaje", "Jugador añadido: " + jugador.getNombre());
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("errorJugadores", e.getMessage());
        }
        return "redirect:/gestion/equipos/" + id + "/editar#jugadores";
    }

    @PostMapping("/jugadores/{id}/nota")
    public String notaJugador(@PathVariable Long id, @RequestParam double nota, RedirectAttributes redireccion) {
        Long equipoId;
        try {
            Jugador jugador = plantillas.cambiarNota(id, nota);
            equipoId = jugador.getEquipo().getId();
            redireccion.addFlashAttribute("mensaje", "Nota de " + jugador.getNombre() + ": "
                    + String.valueOf(jugador.getNota()).replace('.', ','));
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/gestion";
        }
        return "redirect:/gestion/equipos/" + equipoId + "/editar#jugadores";
    }

    @PostMapping("/jugadores/{id}/borrar")
    public String borrarJugador(@PathVariable Long id, RedirectAttributes redireccion) {
        try {
            Jugador jugador = plantillas.borrar(id);
            redireccion.addFlashAttribute("mensaje", "Jugador quitado de la plantilla: " + jugador.getNombre());
            return "redirect:/gestion/equipos/" + jugador.getEquipo().getId() + "/editar#jugadores";
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/gestion";
        }
    }

    @ModelAttribute("deportes")
    public Deporte[] deportes() {
        return Deporte.values();
    }

    @ModelAttribute("formas")
    public Forma[] formas() {
        return Forma.values();
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("competiciones", gestion.competiciones());
        model.addAttribute("equipos", gestion.equipos());
        model.addAttribute("eventos", gestion.eventos());
        model.addAttribute("mercados", mercados.todos());
        model.addAttribute("limites", limites.actuales());
        model.addAttribute("ahora", LocalDateTime.now(reloj));
        model.addAttribute("apiConfigurada", api.configurada());
        return "gestion/panel";
    }

    // --- Límites de apuesta (HU-07) ---

    @GetMapping("/limites")
    public String limites(Model model) {
        Limites actuales = limites.actuales();
        LimitesForm form = new LimitesForm();
        form.setImporteMinimo(actuales.getImporteMinimo());
        form.setImporteMaximo(actuales.getImporteMaximo());
        form.setMaxSelecciones(actuales.getMaxSelecciones());
        model.addAttribute("limites", form);
        return "gestion/limites";
    }

    @PostMapping("/limites")
    public String guardarLimites(@Valid @ModelAttribute("limites") LimitesForm form, BindingResult errores,
                                 RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                limites.cambiar(form.getImporteMinimo(), form.getImporteMaximo(), form.getMaxSelecciones());
                redireccion.addFlashAttribute("mensaje", "Límites de apuesta actualizados");
                return "redirect:/gestion";
            } catch (IllegalArgumentException e) {
                errores.rejectValue("importeMaximo", "invalido", e.getMessage());
            }
        }
        return "gestion/limites";
    }

    /** Activa o desactiva el juego responsable en toda la web (HU-10). */
    @PostMapping("/juego-responsable")
    public String juegoResponsable(@RequestParam boolean activo, RedirectAttributes redireccion) {
        limites.activarJuegoResponsable(activo);
        redireccion.addFlashAttribute("mensaje", activo
                ? "Juego responsable activado: se vuelven a aplicar los límites y pausas de cada usuario."
                : "Juego responsable desactivado: no se aplican los límites ni las pausas de los usuarios.");
        return "redirect:/gestion";
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
        form.setForma(equipo.getForma());
        form.setEscudoUrl(equipo.getEscudoUrl());
        model.addAttribute("edicion", form);
        return formularioEquipo(equipo, model);
    }

    @PostMapping("/equipos/{id}/editar")
    public String guardarEquipo(@PathVariable Long id, @Valid @ModelAttribute("edicion") EdicionEquipoForm form,
                                BindingResult errores, Model model, RedirectAttributes redireccion) {
        Equipo equipo = gestion.equipo(id);
        if (errores.hasErrors()) {
            return formularioEquipo(equipo, model);
        }
        gestion.editarEquipo(id, form);
        redireccion.addFlashAttribute("mensaje", "Equipo actualizado: " + equipo.getNombre());
        return "redirect:/gestion";
    }

    private String formularioEquipo(Equipo equipo, Model model) {
        model.addAttribute("equipo", equipo);
        model.addAttribute("plantilla", plantillas.plantilla(equipo));
        model.addAttribute("posiciones", Posicion.values());
        return "gestion/editar-equipo";
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

    @GetMapping("/eventos/{id}/editar")
    public String editarEvento(@PathVariable Long id, Model model) {
        Evento evento = gestion.evento(id);
        EdicionEventoForm form = new EdicionEventoForm();
        form.setLocalId(evento.getLocal().getId());
        form.setVisitanteId(evento.getVisitante().getId());
        form.setFechaHora(evento.getFechaHora());
        form.setFase(evento.getFase());
        model.addAttribute("edicion", form);
        return formularioEdicionEvento(evento, model);
    }

    @PostMapping("/eventos/{id}/editar")
    public String guardarEvento(@PathVariable Long id, @Valid @ModelAttribute("edicion") EdicionEventoForm form,
                                BindingResult errores, Model model, RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                gestion.editarEvento(id, form);
                redireccion.addFlashAttribute("mensaje", "Evento actualizado");
                return "redirect:/gestion/eventos/" + id;
            } catch (DatosInvalidosException e) {
                errores.rejectValue(e.getCampo(), "invalido", e.getMessage());
            }
        }
        return formularioEdicionEvento(gestion.evento(id), model);
    }

    @PostMapping("/eventos/{id}/borrar")
    public String borrarEvento(@PathVariable Long id, RedirectAttributes redireccion) {
        try {
            gestion.borrarEvento(id);
            redireccion.addFlashAttribute("mensaje", "Evento borrado");
            return "redirect:/gestion";
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/gestion/eventos/" + id;
        }
    }

    private String formularioEdicionEvento(Evento evento, Model model) {
        model.addAttribute("evento", evento);
        model.addAttribute("equipos",
                gestion.equipos().stream().filter(e -> e.participaEn(evento.getCompeticion().getId())).toList());
        model.addAttribute("editable", evento.editable(LocalDateTime.now(reloj)));
        return "gestion/editar-evento";
    }

    @GetMapping("/eventos/{id}")
    public String evento(@PathVariable Long id, Model model) {
        Evento evento = resolucion.evento(id);
        model.addAttribute("evento", evento);
        model.addAttribute("volumen", resolucion.volumen(id));
        model.addAttribute("empezado", evento.haEmpezado(LocalDateTime.now(reloj)));
        model.addAttribute("editable", evento.editable(LocalDateTime.now(reloj)));
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

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

/**
 * Panel del creador de apuestas (solo rol CREADOR, ver SeguridadConfig). Todas sus rutas cuelgan de
 * {@code /gestion}: competiciones, equipos y plantillas (HU-01, HU-50), eventos y su resultado
 * (HU-04, HU-05, HU-21, HU-46), límites de apuesta (HU-07), juego responsable (HU-10) y sincronización
 * con la API. Los mercados a largo plazo tienen su propio controlador, {@link GestionMercadosController}.
 * <p>
 * Patrón de los formularios: si la validación falla o el servicio lanza {@link DatosInvalidosException},
 * se vuelve a mostrar el formulario con el error en su campo; si todo va bien, se redirige con un mensaje
 * (atributo flash) para que recargar la página no repita la acción.
 */
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

    /**
     * POST /gestion/sincronizar: descarga ahora mismo partidos y resultados de la API (HU-21).
     * Si la API no está configurada, o algún partido falla, se avisa con un mensaje de error.
     */
    @PostMapping("/sincronizar")
    public String sincronizar(RedirectAttributes redireccion) {
        try {
            ResumenSincronizacion resumen = sincronizacion.sincronizar();
            // Al concatenar "texto" + resumen, Java llama a resumen.toString()
            redireccion.addFlashAttribute("mensaje", "Sincronizado con la API: " + resumen + ".");
            if (!resumen.getErrores().isEmpty()) {
                // String.join une todos los errores en un solo texto separados por " · "
                redireccion.addFlashAttribute("error", String.join(" · ", resumen.getErrores()));
            }
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion";
    }

    /** POST /gestion/sincronizar-plantillas: descarga ahora mismo las plantillas reales de los equipos (HU-50). */
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

    /**
     * POST /gestion/equipos/{id}/jugadores: añade a mano un jugador a la plantilla del equipo (HU-50).
     * Los parámetros son opcionales para que sea {@link PlantillaService} quien explique qué falta.
     */
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

    /** POST /gestion/jugadores/{id}/nota: cambia la nota (0–10) de un jugador y vuelve a la página de su equipo. */
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

    /** POST /gestion/jugadores/{id}/borrar: quita un jugador de la plantilla. */
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

    /**
     * Deportes para los desplegables; Spring lo añade al modelo de todas las páginas de este controlador.
     * ({@code @ModelAttribute("deportes")} en un método: antes de cada petición de esta clase, Spring lo llama y
     * guarda lo que devuelve como {@code ${deportes}} para la plantilla.)
     */
    @ModelAttribute("deportes")
    public Deporte[] deportes() {
        return Deporte.values();
    }

    /** Formas recientes posibles (HU-02), también para los desplegables. */
    @ModelAttribute("formas")
    public Forma[] formas() {
        return Forma.values();
    }

    /**
     * GET /gestion: página principal del panel, con competiciones, equipos, eventos, mercados, límites
     * y si la API está configurada (si no, la vista lo avisa y desactiva la descarga de plantillas).
     */
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

    /** GET /gestion/limites: formulario de límites de apuesta, relleno con los valores actuales (HU-07). */
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

    /**
     * POST /gestion/limites: guarda los límites de apuesta (HU-07). Las anotaciones del formulario comprueban
     * cada campo; {@link Limites#cambiar} comprueba además que el máximo no sea menor que el mínimo.
     */
    @PostMapping("/limites")
    public String guardarLimites(@Valid @ModelAttribute("limites") LimitesForm form, BindingResult errores,
                                 RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                limites.cambiar(form.getImporteMinimo(), form.getImporteMaximo(), form.getMaxSelecciones());
                redireccion.addFlashAttribute("mensaje", "Límites de apuesta actualizados");
                return "redirect:/gestion";
            } catch (IllegalArgumentException e) {
                // Las reglas entre campos (máximo >= mínimo) se muestran junto al importe máximo
                errores.rejectValue("importeMaximo", "invalido", e.getMessage());
            }
        }
        return "gestion/limites";
    }

    /** POST /gestion/juego-responsable: activa o desactiva el juego responsable en toda la web (HU-10). */
    @PostMapping("/juego-responsable")
    public String juegoResponsable(@RequestParam boolean activo, RedirectAttributes redireccion) {
        limites.activarJuegoResponsable(activo);
        redireccion.addFlashAttribute("mensaje", activo
                ? "Juego responsable activado: se vuelven a aplicar los límites y pausas de cada usuario."
                : "Juego responsable desactivado: no se aplican los límites ni las pausas de los usuarios.");
        return "redirect:/gestion";
    }

    // --- Competiciones ---

    /** GET /gestion/competiciones/nueva: formulario de alta de una competición (HU-01). */
    @GetMapping("/competiciones/nueva")
    public String nuevaCompeticion(Model model) {
        model.addAttribute("competicion", new CompeticionForm());
        return "gestion/competicion";
    }

    /** POST /gestion/competiciones/nueva: crea la competición o vuelve al formulario con el error (HU-01). */
    @PostMapping("/competiciones/nueva")
    public String crearCompeticion(@Valid @ModelAttribute("competicion") CompeticionForm form,
                                   BindingResult errores, RedirectAttributes redireccion) {
        if (errores.hasErrors()) {
            return "gestion/competicion";
        }
        try {
            gestion.crearCompeticion(form);
        } catch (DatosInvalidosException e) {
            errores.rejectValue(e.getCampo(), "invalido", e.getMessage());
            return "gestion/competicion";
        }
        redireccion.addFlashAttribute("mensaje", "Competición creada: " + form.getNombre());
        return "redirect:/gestion";
    }

    // --- Equipos ---

    /** GET /gestion/equipos/nuevo: formulario de alta de un equipo o deportista (HU-01). */
    @GetMapping("/equipos/nuevo")
    public String nuevoEquipo(Model model) {
        model.addAttribute("equipo", new EquipoForm());
        model.addAttribute("competiciones", gestion.competiciones());
        return "gestion/equipo";
    }

    /** POST /gestion/equipos/nuevo: crea el equipo o vuelve al formulario con el error (HU-01). */
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

    /**
     * GET /gestion/equipos/{id}/editar: formulario para cambiar calificación, forma y escudo, junto con
     * la plantilla del equipo (HU-50).
     */
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

    /** POST /gestion/equipos/{id}/editar: guarda calificación, forma y escudo del equipo. */
    @PostMapping("/equipos/{id}/editar")
    public String guardarEquipo(@PathVariable Long id, @Valid @ModelAttribute("edicion") EdicionEquipoForm form,
                                BindingResult errores, Model model, RedirectAttributes redireccion) {
        // Se carga antes de validar porque la página de edición lo necesita si hay errores
        Equipo equipo = gestion.equipo(id);
        if (errores.hasErrors()) {
            return formularioEquipo(equipo, model);
        }
        gestion.editarEquipo(id, form);
        redireccion.addFlashAttribute("mensaje", "Equipo actualizado: " + equipo.getNombre());
        return "redirect:/gestion";
    }

    /** Prepara el modelo de la página de edición de un equipo (datos, plantilla y posiciones). */
    private String formularioEquipo(Equipo equipo, Model model) {
        model.addAttribute("equipo", equipo);
        model.addAttribute("plantilla", plantillas.plantilla(equipo));
        model.addAttribute("posiciones", Posicion.values());
        return "gestion/editar-equipo";
    }

    // --- Eventos ---

    /** GET /gestion/eventos/nuevo: formulario para crear un evento a mano (HU-21). */
    @GetMapping("/eventos/nuevo")
    public String nuevoEvento(Model model) {
        model.addAttribute("evento", new EventoForm());
        return formularioEvento(model);
    }

    /** POST /gestion/eventos/nuevo: crea el evento o vuelve al formulario con el error (HU-21). */
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

    /** GET /gestion/eventos/{id}/editar: formulario de edición de un evento, relleno con sus datos (HU-46). */
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

    /**
     * POST /gestion/eventos/{id}/editar: guarda los cambios de fecha, equipos o fase (HU-46).
     * Las reglas (evento sin empezar, equipos fijos si ya hay apuestas) están en {@link GestionService#editarEvento}.
     */
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
        // La página de edición necesita el evento para mostrar sus datos junto a los errores
        return formularioEdicionEvento(gestion.evento(id), model);
    }

    /**
     * POST /gestion/eventos/{id}/borrar: borra un evento sin apuestas (HU-46). Si tiene apuestas no se borra
     * y se muestra el motivo: el creador tiene que anularlo.
     */
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

    /**
     * Prepara el modelo de la página de edición. Solo se ofrecen los equipos de la competición del evento,
     * y {@code editable} indica a la vista si el formulario se puede enviar (evento sin empezar).
     */
    private String formularioEdicionEvento(Evento evento, Model model) {
        model.addAttribute("evento", evento);
        model.addAttribute("equipos",
                gestion.equipos().stream().filter(e -> e.participaEn(evento.getCompeticion().getId())).toList());
        model.addAttribute("editable", evento.editable(LocalDateTime.now(reloj)));
        return "gestion/editar-evento";
    }

    /**
     * GET /gestion/eventos/{id}: ficha del evento para el creador, con el dinero apostado a cada opción (HU-06)
     * y los botones de resultado, suspender, reactivar, anular, editar y borrar.
     */
    @GetMapping("/eventos/{id}")
    public String evento(@PathVariable Long id, Model model) {
        Evento evento = resolucion.evento(id);
        model.addAttribute("evento", evento);
        model.addAttribute("volumen", resolucion.volumen(id));
        model.addAttribute("empezado", evento.haEmpezado(LocalDateTime.now(reloj)));
        model.addAttribute("editable", evento.editable(LocalDateTime.now(reloj)));
        return "gestion/evento-detalle";
    }

    /**
     * POST /gestion/eventos/{id}/resultado: introduce o corrige el resultado y resuelve las apuestas (HU-04).
     * Si llega el marcador completo se usa el marcador, que además resuelve goles y ambos marcan (HU-52);
     * si solo llega el ganador, se usa el ganador. Los errores se muestran en la ficha del evento.
     */
    @PostMapping("/eventos/{id}/resultado")
    public String resultado(@PathVariable Long id, @RequestParam(required = false) Resultado resultado,
                            @RequestParam(required = false) Integer golesLocal,
                            @RequestParam(required = false) Integer golesVisitante,
                            RedirectAttributes redireccion) {
        try {
            // Se declaran aquí y se rellenan en cada rama del if: Java comprueba que ninguna rama los deja sin valor
            String guardado;
            int resueltas;
            // Con los dos goles manda el marcador; el ganador se deduce de él
            if (golesLocal != null && golesVisitante != null) {
                Resultado delMarcador = golesLocal > golesVisitante ? Resultado.LOCAL
                        : golesLocal < golesVisitante ? Resultado.VISITANTE : Resultado.EMPATE;
                if (resultado != null && resultado != delMarcador) {
                    // Al corregir, el marcador viene relleno con el anterior: no se ignora en silencio el ganador marcado
                    throw new IllegalArgumentException("El marcador " + golesLocal + "-" + golesVisitante
                            + " no coincide con el resultado marcado (" + resultado.getDescripcion()
                            + "). Corrige el marcador o deja marcado solo uno de los dos.");
                }
                resueltas = resolucion.introducirMarcador(id, golesLocal, golesVisitante);
                guardado = golesLocal + "-" + golesVisitante;
            } else if (resultado != null) {
                resueltas = resolucion.introducirResultado(id, resultado);
                guardado = resultado.getDescripcion();
            } else {
                throw new IllegalArgumentException("Indica el marcador o el ganador");
            }
            redireccion.addFlashAttribute("mensaje",
                    "Resultado guardado: " + guardado + ". Apuestas resueltas: " + resueltas + ".");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/eventos/" + id;
    }

    /** POST /gestion/eventos/{id}/suspender: el evento deja de admitir apuestas nuevas (HU-05). */
    @PostMapping("/eventos/{id}/suspender")
    public String suspender(@PathVariable Long id, RedirectAttributes redireccion) {
        // () -> resolucion.suspender(id) es una lambda sin parámetros: se le pasa a accion() "lo que hay que hacer",
        // y accion() lo ejecuta dentro de su try/catch. Así no se repite el mismo try/catch en los tres métodos
        return accion(id, () -> resolucion.suspender(id), "Evento suspendido: no admite apuestas nuevas.", redireccion);
    }

    /** POST /gestion/eventos/{id}/reactivar: un evento suspendido vuelve a admitir apuestas (HU-05). */
    @PostMapping("/eventos/{id}/reactivar")
    public String reactivar(@PathVariable Long id, RedirectAttributes redireccion) {
        return accion(id, () -> resolucion.reactivar(id), "Evento reactivado.", redireccion);
    }

    /** POST /gestion/eventos/{id}/anular: anula el evento y devuelve el importe de sus apuestas (HU-05). */
    @PostMapping("/eventos/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes redireccion) {
        return accion(id, () -> resolucion.anular(id),
                "Evento anulado. Se ha devuelto el importe de todas sus apuestas.", redireccion);
    }

    /**
     * Ejecuta una acción sobre el evento y vuelve a su ficha. Si el estado del evento no la permite
     * (por ejemplo, suspender uno ya finalizado), muestra el error en lugar de fallar.
     */
    private String accion(Long id, Runnable accion, String mensaje, RedirectAttributes redireccion) {
        try {
            // Runnable es una interfaz con un solo método, run(): ejecuta la lambda que se pasó
            accion.run();
            redireccion.addFlashAttribute("mensaje", mensaje);
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion/eventos/" + id;
    }

    /** Prepara el modelo del formulario de alta de evento (competiciones y equipos para los desplegables). */
    private String formularioEvento(Model model) {
        model.addAttribute("competiciones", gestion.competiciones());
        model.addAttribute("equipos", gestion.equipos());
        return "gestion/evento";
    }
}

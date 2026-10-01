package es.ucm.fdi.is1.apuestas.gestion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import jakarta.validation.Valid;

/** Panel del creador de apuestas (solo rol CREADOR, ver SeguridadConfig). */
@Controller
@RequestMapping("/gestion")
public class GestionController {

    private final GestionService gestion;

    public GestionController(GestionService gestion) {
        this.gestion = gestion;
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

    @GetMapping("/equipos/{id}/escudo")
    public String editarEscudo(@PathVariable Long id, Model model) {
        Equipo equipo = gestion.equipo(id);
        EscudoForm form = new EscudoForm();
        form.setEscudoUrl(equipo.getEscudoUrl());
        model.addAttribute("equipo", equipo);
        model.addAttribute("escudo", form);
        return "gestion/escudo";
    }

    @PostMapping("/equipos/{id}/escudo")
    public String guardarEscudo(@PathVariable Long id, @Valid @ModelAttribute("escudo") EscudoForm form,
                                BindingResult errores, Model model, RedirectAttributes redireccion) {
        Equipo equipo = gestion.equipo(id);
        if (errores.hasErrors()) {
            model.addAttribute("equipo", equipo);
            return "gestion/escudo";
        }
        gestion.cambiarEscudo(id, form.getEscudoUrl());
        redireccion.addFlashAttribute("mensaje", "Escudo actualizado: " + equipo.getNombre());
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

    private String formularioEvento(Model model) {
        model.addAttribute("competiciones", gestion.competiciones());
        model.addAttribute("equipos", gestion.equipos());
        return "gestion/evento";
    }
}

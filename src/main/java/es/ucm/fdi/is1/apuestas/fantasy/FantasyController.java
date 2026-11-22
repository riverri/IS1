package es.ucm.fdi.is1.apuestas.fantasy;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;
import es.ucm.fdi.is1.apuestas.fantasy.FantasyService.Jornada;
import es.ucm.fdi.is1.apuestas.fantasy.FantasyService.PuntosFichaje;

/** Páginas de la liga fantasy (HU-81 a HU-83). Hace falta haber iniciado sesión (ver SeguridadConfig). */
@Controller
public class FantasyController {

    /** Día y mes, para el lunes en que empieza la jornada siguiente. */
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("d/M");

    private final FantasyService fantasy;

    public FantasyController(FantasyService fantasy) {
        this.fantasy = fantasy;
    }

    /** Un sitio de la alineación: el fichaje que lo ocupa, con sus puntos, o null si está libre. */
    public record Hueco(Posicion posicion, PuntosFichaje ocupante) {
    }

    /** Un jugador del mercado con su precio y, si no se puede fichar, por qué. */
    public record Oferta(Jugador jugador, BigDecimal precio, String motivo) {
    }

    /** GET /fantasy: mi equipo (o el formulario para crearlo) y la clasificación. */
    @GetMapping("/fantasy")
    public String inicio(Principal principal, Model model) {
        Optional<EquipoFantasy> equipo = fantasy.equipoDe(principal.getName());
        model.addAttribute("clasificacion", fantasy.clasificacion(principal.getName()));
        model.addAttribute("presupuesto", EquipoFantasy.PRESUPUESTO);
        model.addAttribute("maximoPorClub", EquipoFantasy.MAXIMO_POR_CLUB);
        model.addAttribute("jornada", fantasy.jornada());
        if (equipo.isEmpty()) {
            model.addAttribute("equipo", null);
            return "fantasy";
        }
        List<PuntosFichaje> puntos = fantasy.puntos(equipo.get());
        List<Hueco> huecos = new ArrayList<>();
        for (Map.Entry<Posicion, Integer> plazas : EquipoFantasy.FORMACION.entrySet()) {
            List<PuntosFichaje> ocupantes = puntos.stream()
                    .filter(p -> p.fichaje().isActivo() && p.fichaje().getPosicion() == plazas.getKey())
                    .toList();
            for (int i = 0; i < plazas.getValue(); i++) {
                huecos.add(new Hueco(plazas.getKey(), i < ocupantes.size() ? ocupantes.get(i) : null));
            }
        }
        model.addAttribute("equipo", equipo.get());
        model.addAttribute("huecos", huecos);
        model.addAttribute("vendidos", puntos.stream().filter(p -> !p.fichaje().isActivo()).toList());
        model.addAttribute("total", puntos.stream().mapToInt(PuntosFichaje::getTotal).sum());
        return "fantasy";
    }

    /** GET /fantasy/mercado: los futbolistas que se pueden fichar, con filtros por posición y club. */
    @GetMapping("/fantasy/mercado")
    public String mercado(@RequestParam(required = false) Posicion posicion, @RequestParam(required = false) Long club,
                          @RequestParam(defaultValue = "0") int pagina, Principal principal, Model model) {
        Optional<EquipoFantasy> equipo = fantasy.equipoDe(principal.getName());
        if (equipo.isEmpty()) {
            return "redirect:/fantasy";
        }
        model.addAttribute("equipo", equipo.get());
        model.addAttribute("jornada", fantasy.jornada());
        var resultado = fantasy.paginaMercado(posicion, club, pagina);
        model.addAttribute("ofertas", resultado.jugadores().stream()
                .map(j -> new Oferta(j, EquipoFantasy.precio(j), equipo.get().motivoParaNoFichar(j))).toList());
        model.addAttribute("pagina", resultado);
        model.addAttribute("posiciones", Posicion.values());
        model.addAttribute("clubes", fantasy.clubes());
        model.addAttribute("posicion", posicion);
        model.addAttribute("club", club);
        return "fantasy-mercado";
    }

    @PostMapping("/fantasy/crear")
    public String crear(@RequestParam(required = false) String nombre, Principal principal,
                        RedirectAttributes redireccion) {
        try {
            fantasy.crear(principal.getName(), nombre);
            redireccion.addFlashAttribute("mensaje", "Equipo creado. Ahora ficha a tus siete jugadores.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            // Doble clic: el otro envío ya lo ha creado (la base de datos no deja dos equipos por usuario)
            redireccion.addFlashAttribute("error", "Ya tienes un equipo");
        }
        return "redirect:/fantasy";
    }

    /** POST /fantasy/fichar: vuelve al mercado con los mismos filtros, o a mi equipo si ya está completo. */
    @PostMapping("/fantasy/fichar")
    public String fichar(@RequestParam Long jugador, @RequestParam(required = false) Posicion posicion,
                         @RequestParam(required = false) Long club,
                         @RequestParam(defaultValue = "0") int pagina, Principal principal,
                         RedirectAttributes redireccion) {
        try {
            FichajeFantasy fichaje = fantasy.fichar(principal.getName(), jugador);
            Jornada jornada = fantasy.jornada();
            redireccion.addFlashAttribute("mensaje", "Has fichado a " + fichaje.getNombreJugador() + "."
                    + (jornada.empezada() ? " Como la jornada ya ha empezado, suma desde el lunes "
                    + DIA.format(jornada.fin()) + "." : ""));
            if (fichaje.getEquipo().isCompleto()) {
                return "redirect:/fantasy";
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        if (posicion != null) {
            redireccion.addAttribute("posicion", posicion);
        }
        if (club != null) {
            redireccion.addAttribute("club", club);
        }
        if (pagina > 0) {
            redireccion.addAttribute("pagina", pagina);
        }
        return "redirect:/fantasy/mercado";
    }

    /** POST /fantasy/vender: por el fichaje (sirve aunque el jugador ya no exista) o, si no viene, por el jugador. */
    @PostMapping("/fantasy/vender")
    public String vender(@RequestParam(required = false) Long fichaje, @RequestParam(required = false) Long jugador,
                         Principal principal, RedirectAttributes redireccion) {
        try {
            FichajeFantasy vendido = fichaje != null ? fantasy.venderFichaje(principal.getName(), fichaje)
                    : fantasy.vender(principal.getName(), jugador);
            Jornada jornada = fantasy.jornada();
            redireccion.addFlashAttribute("mensaje", "Has vendido a " + vendido.getNombreJugador()
                    + ". Los puntos que hizo contigo se quedan."
                    + (jornada.empezada() ? " Como la jornada ya ha empezado, sigue sumando hasta que acabe." : ""));
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/fantasy";
    }
}

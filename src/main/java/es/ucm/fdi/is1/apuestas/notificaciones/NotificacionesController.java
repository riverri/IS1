package es.ucm.fdi.is1.apuestas.notificaciones;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de la página de avisos del usuario (HU-37). Al abrirla, los avisos que se ven quedan leídos.
 * Exige haber iniciado sesión ({@code SeguridadConfig}).
 */
@Controller
public class NotificacionesController {

    private final NotificacionService notificaciones;

    public NotificacionesController(NotificacionService notificaciones) {
        this.notificaciones = notificaciones;
    }

    /** GET /notificaciones: los últimos avisos del usuario, con los nuevos destacados. */
    @GetMapping("/notificaciones")
    public String notificaciones(Principal principal, Model model) {
        model.addAttribute("avisos", notificaciones.leer(principal.getName()));
        // La cabecera ya no tiene avisos pendientes: el recuento de UsuarioActualAdvice se hizo antes de leerlos
        model.addAttribute("avisosSinLeer", 0L);
        return "notificaciones";
    }
}

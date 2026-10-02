package es.ucm.fdi.is1.apuestas.notificaciones;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Página de avisos del usuario (HU-37). Al abrirla, los avisos quedan leídos. */
@Controller
public class NotificacionesController {

    private final NotificacionService notificaciones;

    public NotificacionesController(NotificacionService notificaciones) {
        this.notificaciones = notificaciones;
    }

    @GetMapping("/notificaciones")
    public String notificaciones(Principal principal, Model model) {
        model.addAttribute("avisos", notificaciones.leer(principal.getName()));
        // La cabecera ya no tiene avisos pendientes
        model.addAttribute("avisosSinLeer", 0L);
        return "notificaciones";
    }
}

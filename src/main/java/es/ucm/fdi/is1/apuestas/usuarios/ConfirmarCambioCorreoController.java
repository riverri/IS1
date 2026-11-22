package es.ucm.fdi.is1.apuestas.usuarios;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Confirms email changes only after an explicit POST from the mailbox owner. */
@Controller
public class ConfirmarCambioCorreoController {

    private final CambioCorreoService cambios;
    private final Sesiones sesiones;

    public ConfirmarCambioCorreoController(CambioCorreoService cambios, Sesiones sesiones) {
        this.cambios = cambios;
        this.sesiones = sesiones;
    }

    @GetMapping("/cuenta/correo/confirmar")
    public String confirmar(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("valido", cambios.valido(token));
        return "confirmar-cambio-correo";
    }

    @PostMapping("/cuenta/correo/confirmar")
    public String consumir(@RequestParam String token, RedirectAttributes redireccion) {
        try {
            String emailAnterior = cambios.confirmar(token);
            sesiones.cerrarOtras(emailAnterior, null);
            return "redirect:/login?correoCambiado";
        } catch (EnlaceVerificacionNoValidoException | EmailYaRegistradoException e) {
            redireccion.addFlashAttribute("error", e instanceof EmailYaRegistradoException
                    ? "Ese correo ya está asociado a otra cuenta; solicita un enlace nuevo con otra dirección."
                    : e.getMessage());
            return "redirect:/cuenta/correo/confirmar";
        }
    }
}

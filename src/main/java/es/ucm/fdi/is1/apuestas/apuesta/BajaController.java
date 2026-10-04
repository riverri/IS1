package es.ucm.fdi.is1.apuestas.apuesta;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.usuarios.PasswordIncorrectaException;
import es.ucm.fdi.is1.apuestas.usuarios.Sesiones;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/** Eliminar la propia cuenta desde Mi cuenta (HU-18). */
@Controller
public class BajaController {

    private final BajaService bajas;

    private final Sesiones sesiones;

    public BajaController(BajaService bajas, Sesiones sesiones) {
        this.bajas = bajas;
        this.sesiones = sesiones;
    }

    @PostMapping("/cuenta/eliminar")
    public String eliminar(@RequestParam(required = false) String password, Principal principal,
                           HttpServletRequest peticion, RedirectAttributes redireccion) throws ServletException {
        try {
            bajas.eliminarCuenta(principal.getName(), password);
        } catch (PasswordIncorrectaException e) {
            redireccion.addFlashAttribute("errorBaja", "La contraseña no es correcta: la cuenta no se ha eliminado.");
            return "redirect:/cuenta#eliminar-cuenta";
        } catch (IllegalStateException e) {
            redireccion.addFlashAttribute("errorBaja", e.getMessage());
            return "redirect:/cuenta#eliminar-cuenta";
        }
        sesiones.cerrarOtras(principal.getName(), peticion.getSession(false));
        peticion.logout();
        return "redirect:/?cuentaEliminada";
    }
}

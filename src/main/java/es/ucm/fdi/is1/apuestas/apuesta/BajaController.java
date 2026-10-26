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

    /**
     * POST /cuenta/eliminar: pide la contraseña para confirmar. Si es correcta, elimina la cuenta, cierra
     * todas las sesiones del usuario y lleva a la portada; si no, vuelve a Mi cuenta con el error.
     */
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
        // Se cierran las sesiones abiertas en otros navegadores y después la actual (CA-18.2)
        sesiones.cerrarOtras(principal.getName(), peticion.getSession(false));
        // Cierra la sesión de este navegador (como pulsar "Salir")
        peticion.logout();
        return "redirect:/?cuentaEliminada";
    }
}

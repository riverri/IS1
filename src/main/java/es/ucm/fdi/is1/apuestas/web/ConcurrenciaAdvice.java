package es.ucm.fdi.is1.apuestas.web;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Dos operaciones a la vez sobre el mismo saldo (doble clic, dos pestañas…): la segunda se rechaza
 * y se pide repetirla, en lugar de mostrar un error 500.
 */
@ControllerAdvice
public class ConcurrenciaAdvice {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String aLaVez(@RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redireccion) {
        redireccion.addFlashAttribute("error",
                "Se ha hecho otra operación con tu saldo al mismo tiempo. Comprueba tus apuestas e inténtalo de nuevo.");
        return "redirect:" + RutaSegura.de(referer, "/apuestas");
    }
}

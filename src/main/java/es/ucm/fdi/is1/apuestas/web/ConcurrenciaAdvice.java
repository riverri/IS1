package es.ucm.fdi.is1.apuestas.web;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Dos operaciones a la vez sobre el mismo saldo (doble clic, dos pestañas…): la segunda se rechaza
 * y se pide repetirla, en lugar de mostrar un error 500.
 * {@code @ControllerAdvice}: se aplica a todos los controladores. La excepción la lanza JPA cuando falla
 * el control de versión ({@code @Version}) de {@code Usuario}: otra operación cambió el saldo antes.
 *
 * <p>También le puede pasar al creador: al guardar un resultado se cambian los saldos de todos los que
 * apostaron, y si uno de ellos estaba apostando o cancelando justo entonces, se deshace todo el resultado
 * (así ningún saldo queda a medias). Por eso en Gestión el aviso es otro.
 */
@ControllerAdvice
public class ConcurrenciaAdvice {

    static final String AVISO_JUGADOR =
            "Se ha hecho otra operación con tu saldo al mismo tiempo. Comprueba tus apuestas e inténtalo de nuevo.";
    static final String AVISO_GESTION = "Un apostante estaba operando con su saldo justo en ese momento. "
            + "No se ha guardado nada: vuelve a intentarlo.";

    /**
     * Vuelve a la página de la que venía (Referer) con el aviso; si no se sabe, a Mis apuestas (/apuestas)
     * o, desde Gestión, al panel (/gestion).
     * {@code @ExceptionHandler}: si cualquier controlador deja escapar esa excepción, Spring llama a este método
     * en lugar de mostrar la página de error.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String aLaVez(@RequestHeader(value = "Referer", required = false) String referer,
                         HttpServletRequest peticion, RedirectAttributes redireccion) {
        boolean desdeGestion = peticion.getRequestURI().startsWith(peticion.getContextPath() + "/gestion");
        redireccion.addFlashAttribute("error", desdeGestion ? AVISO_GESTION : AVISO_JUGADOR);
        // RutaSegura: el Referer lo manda el navegador y no hay que fiarse de él
        return "redirect:" + RutaSegura.de(referer, desdeGestion ? "/gestion" : "/apuestas");
    }
}

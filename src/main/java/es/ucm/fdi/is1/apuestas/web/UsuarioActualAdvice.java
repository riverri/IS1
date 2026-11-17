package es.ucm.fdi.is1.apuestas.web;

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import es.ucm.fdi.is1.apuestas.apuesta.Boleto;
import es.ucm.fdi.is1.apuestas.apuesta.BoletoService;
import es.ucm.fdi.is1.apuestas.apuesta.BoletoVista;
import es.ucm.fdi.is1.apuestas.apuesta.LimitesService;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Datos comunes a todas las vistas: usuario con sesión iniciada (cabecera, saldo) y su boleto.
 * {@code @ControllerAdvice}: cada método {@code @ModelAttribute} se ejecuta antes de cualquier controlador
 * y deja su resultado en el modelo con ese nombre, así las plantillas lo tienen siempre a mano.
 * Para los visitantes ({@code principal} null) se devuelven valores vacíos.
 * Por ejemplo, gracias a {@link #usuarioActual} la cabecera de {@code fragments/comun.html} puede pintar
 * {@code ${usuarioActual.saldo}} en cualquier página sin que cada controlador lo añada a mano.
 */
@ControllerAdvice
public class UsuarioActualAdvice {

    private final UsuarioService usuarios;
    // Boleto es de sesión: Spring inyecta un intermediario que lleva al boleto de la sesión de cada petición
    private final Boleto boleto;
    private final BoletoService boletos;
    private final NotificacionService notificaciones;
    private final LimitesService limites;
    private final Clock reloj;

    public UsuarioActualAdvice(UsuarioService usuarios, Boleto boleto, BoletoService boletos,
                               NotificacionService notificaciones, LimitesService limites, Clock reloj) {
        this.usuarios = usuarios;
        this.boleto = boleto;
        this.boletos = boletos;
        this.notificaciones = notificaciones;
        this.limites = limites;
        this.reloj = reloj;
    }

    /** Ruta de la página actual, para marcar la sección activa en el menú. */
    @ModelAttribute("ruta")
    public String ruta(HttpServletRequest peticion) {
        return peticion.getRequestURI();
    }

    /** Si el creador ha desactivado el juego responsable, Mi cuenta no lo muestra (HU-10). */
    @ModelAttribute("juegoResponsableActivo")
    public boolean juegoResponsableActivo() {
        return limites.actuales().isJuegoResponsable();
    }

    /** Fin de la pausa de apuestas del usuario, o null si no tiene una activa (HU-10). */
    @ModelAttribute("pausaHasta")
    public LocalDateTime pausaHasta(Principal principal) {
        if (principal == null || !limites.actuales().isJuegoResponsable()) {
            return null;
        }
        return usuarios.buscar(principal.getName())
                .filter(u -> u.enPausa(LocalDateTime.now(reloj)))
                .map(u -> u.getPausaHasta())
                .orElse(null);
    }

    /** Número de avisos sin leer, para la campana de la cabecera (HU-37). 0 para los visitantes. */
    @ModelAttribute("avisosSinLeer")
    public Long avisosSinLeer(Principal principal) {
        return principal == null ? 0L : notificaciones.sinLeer(principal.getName());
    }

    /**
     * El boleto en construcción, para marcar las cuotas elegidas en el catálogo. Los visitantes no tienen
     * boleto: así no se les crea una sesión, que fallaría si parte de la página ya se ha enviado al navegador.
     */
    @ModelAttribute("boleto")
    public Boleto boleto(Principal principal) {
        return principal == null ? null : boleto;
    }

    /** Resumen para la barra del boleto: número de selecciones y multiplicador. Null si está vacío. */
    @ModelAttribute("boletoResumen")
    public BoletoVista boletoResumen(Principal principal) {
        if (principal == null || boleto.isVacio()) {
            return null;
        }
        return boletos.resumen(boleto);
    }

    /**
     * Usuario con sesión iniciada, para la cabecera (nombre, saldo); null para los visitantes.
     * No aplica la recarga gratuita: eso se hace al entrar y en Mi cuenta.
     */
    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarios.buscar(principal.getName()).orElse(null);
    }
}

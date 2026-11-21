package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.web.Monedas;

/**
 * Avisa al usuario cuando una de sus apuestas se gana, se pierde o se anula (HU-37). La usan
 * {@link ResolucionService} y {@link ResolucionMercadosService} después de resolver cada apuesta;
 * el aviso se guarda con {@link NotificacionService}. Al ganar, además, se manda un correo (HU-77, {@link AvisoPorCorreo}).
 * Solo es visible dentro del paquete.
 */
@Component
class AvisosApuestas {

    private final NotificacionService notificaciones;
    private final ApplicationEventPublisher eventos;

    AvisosApuestas(NotificacionService notificaciones, ApplicationEventPublisher eventos) {
        this.notificaciones = notificaciones;
        this.eventos = eventos;
    }

    /**
     * Solo si el estado ha cambiado; una combinada que sigue pendiente de otros partidos no avisa.
     *
     * @param pagadoAntes lo que se le había pagado por la apuesta antes de resolverla ahora: en una corrección se le
     *                    dice la diferencia que de verdad se le abona o retira, no el premio o el importe entero
     */
    void siCambia(Apuesta apuesta, EstadoApuesta antes, BigDecimal pagadoAntes) {
        EstadoApuesta ahora = apuesta.getEstado();
        // Una cuenta eliminada no lee avisos ni cobra (abonar no hace nada): no se le avisa ni se le manda correo
        if (ahora == antes || ahora == EstadoApuesta.ACTIVA || ahora == EstadoApuesta.CANCELADA
                || apuesta.getUsuario().isEliminado()) {
            return;
        }
        String texto;
        if (antes == EstadoApuesta.ACTIVA) {
            texto = switch (ahora) {
                case GANADA -> "¡Has acertado " + descripcion(apuesta) + "! Cobras "
                        + Monedas.texto(apuesta.getGananciaPotencial()) + " monedas.";
                case PERDIDA -> "Has perdido " + descripcion(apuesta) + ".";
                default -> "Se ha anulado " + descripcion(apuesta) + ". Te hemos devuelto "
                        + Monedas.texto(apuesta.getImporte()) + " monedas.";
            };
        } else {
            // Ya estaba resuelta: es una corrección del resultado (HU-04) y solo se mueve la diferencia
            String estado = switch (ahora) {
                case GANADA -> "Ahora ganas " + descripcion(apuesta) + ".";
                case PERDIDA -> "Ahora pierdes " + descripcion(apuesta) + ".";
                default -> "Se ha anulado " + descripcion(apuesta) + ".";
            };
            BigDecimal diferencia = apuesta.getPagado().subtract(pagadoAntes);
            String dinero = diferencia.signum() > 0 ? " Te hemos abonado " + Monedas.texto(diferencia) + " monedas."
                    : diferencia.signum() < 0 ? " Te hemos retirado " + Monedas.texto(diferencia.negate()) + " monedas."
                    : "";
            texto = "Resultado corregido. " + estado + dinero;
        }
        // El tipo del aviso es el estado en minúsculas: "ganada", "perdida" o "anulada"
        notificaciones.avisar(apuesta.getUsuario(), ahora.name().toLowerCase(Locale.ROOT), texto);
        // HU-77: correo solo al ganar por primera vez, no al corregir, y si el usuario no lo ha desactivado.
        // AvisoPorCorreo lo manda cuando se guarde la transacción
        Usuario usuario = apuesta.getUsuario();
        if (ahora == EstadoApuesta.GANADA && antes == EstadoApuesta.ACTIVA && usuario.isCorreosActivados()) {
            eventos.publishEvent(new PremioGanado(usuario.getEmail(), usuario.getNombre(), texto));
        }
    }

    /** "tu apuesta a Real Madrid (Real Madrid – Getafe CF)" o "tu combinada de 3 selecciones". */
    private static String descripcion(Apuesta apuesta) {
        if (apuesta.isCombinada()) {
            return "tu combinada de " + apuesta.getSelecciones().size() + " selecciones";
        }
        Seleccion seleccion = apuesta.getSelecciones().get(0);
        return "tu apuesta a " + seleccion.getDescripcionPronostico() + " (" + seleccion.getTitulo() + ")";
    }

    /** Importe en formato español: "1.234,50". */
}

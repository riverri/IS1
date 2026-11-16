package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

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

    /** Solo si el estado ha cambiado; una combinada que sigue pendiente de otros partidos no avisa. */
    void siCambia(Apuesta apuesta, EstadoApuesta antes) {
        EstadoApuesta ahora = apuesta.getEstado();
        if (ahora == antes || ahora == EstadoApuesta.ACTIVA || ahora == EstadoApuesta.CANCELADA) {
            return;
        }
        // Si ya estaba resuelta, es una corrección del resultado (HU-04) y se dice así
        String correccion = antes == EstadoApuesta.ACTIVA ? "" : "Resultado corregido. ";
        String texto = switch (ahora) {
            case GANADA -> correccion + "¡Has acertado " + descripcion(apuesta) + "! Cobras "
                    + monedas(apuesta.getGananciaPotencial()) + " monedas.";
            case PERDIDA -> correccion + "Has perdido " + descripcion(apuesta) + ".";
            default -> correccion + "Se ha anulado " + descripcion(apuesta) + ". Te hemos devuelto "
                    + monedas(apuesta.getImporte()) + " monedas.";
        };
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
    private static String monedas(BigDecimal cantidad) {
        // Patrón "#,##0.00": separador de miles y siempre 2 decimales. Con los símbolos españoles, la coma de
        // miles se pinta como punto y el punto decimal como coma
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-ES")));
        formato.setGroupingSize(3);
        formato.setGroupingUsed(true);
        return formato.format(cantidad);
    }
}

package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.springframework.stereotype.Component;

import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;

/** Avisa al usuario cuando una de sus apuestas se gana, se pierde o se anula (HU-37). */
@Component
class AvisosApuestas {

    private final NotificacionService notificaciones;

    AvisosApuestas(NotificacionService notificaciones) {
        this.notificaciones = notificaciones;
    }

    /** Solo si el estado ha cambiado; una combinada que sigue pendiente de otros partidos no avisa. */
    void siCambia(Apuesta apuesta, EstadoApuesta antes) {
        EstadoApuesta ahora = apuesta.getEstado();
        if (ahora == antes || ahora == EstadoApuesta.ACTIVA || ahora == EstadoApuesta.CANCELADA) {
            return;
        }
        String correccion = antes == EstadoApuesta.ACTIVA ? "" : "Resultado corregido. ";
        String texto = switch (ahora) {
            case GANADA -> correccion + "¡Has acertado " + descripcion(apuesta) + "! Cobras "
                    + monedas(apuesta.getGananciaPotencial()) + " monedas.";
            case PERDIDA -> correccion + "Has perdido " + descripcion(apuesta) + ".";
            default -> correccion + "Se ha anulado " + descripcion(apuesta) + ". Te hemos devuelto "
                    + monedas(apuesta.getImporte()) + " monedas.";
        };
        notificaciones.avisar(apuesta.getUsuario(), ahora.name().toLowerCase(Locale.ROOT), texto);
    }

    /** "tu apuesta a Real Madrid (Real Madrid – Getafe CF)" o "tu combinada de 3 selecciones". */
    private static String descripcion(Apuesta apuesta) {
        if (apuesta.isCombinada()) {
            return "tu combinada de " + apuesta.getSelecciones().size() + " selecciones";
        }
        Seleccion seleccion = apuesta.getSelecciones().get(0);
        return "tu apuesta a " + seleccion.getDescripcionPronostico() + " (" + seleccion.getTitulo() + ")";
    }

    private static String monedas(BigDecimal cantidad) {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-ES")));
        formato.setGroupingSize(3);
        formato.setGroupingUsed(true);
        return formato.format(cantidad);
    }
}

package es.ucm.fdi.is1.apuestas.apuesta;

import java.util.List;

/**
 * Alguna cuota del boleto ha cambiado desde que el usuario la vio (HU-29). La lanza {@link ApuestaService}
 * antes de cobrar nada; el controlador la captura y enseña las cuotas nuevas para que el usuario decida.
 */
public class CuotasCambiadasException extends RuntimeException {

    /** Ids de los eventos cuya cuota ha cambiado; vacía en las apuestas a largo plazo. */
    private final List<Long> eventos;

    public CuotasCambiadasException(List<Long> eventos) {
        super("Han cambiado las cuotas de algunas selecciones");
        this.eventos = List.copyOf(eventos);
    }

    public List<Long> getEventos() {
        return eventos;
    }
}

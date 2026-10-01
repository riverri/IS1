package es.ucm.fdi.is1.apuestas.apuesta;

import java.util.List;

/** Alguna cuota del boleto ha cambiado desde que el usuario la vio (HU-29). */
public class CuotasCambiadasException extends RuntimeException {

    private final List<Long> eventos;

    public CuotasCambiadasException(List<Long> eventos) {
        super("Han cambiado las cuotas de algunas selecciones");
        this.eventos = List.copyOf(eventos);
    }

    public List<Long> getEventos() {
        return eventos;
    }
}

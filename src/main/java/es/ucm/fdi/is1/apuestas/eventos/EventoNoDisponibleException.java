package es.ucm.fdi.is1.apuestas.eventos;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * El evento pedido no existe o ya no admite apuestas (suspendido, empezado, finalizado o anulado).
 * La lanzan {@link CatalogoService} y los servicios de apuestas y de gestión; si nadie la captura,
 * Spring responde con un 404 gracias a {@code @ResponseStatus}.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EventoNoDisponibleException extends RuntimeException {

    public EventoNoDisponibleException(Long id) {
        super("El evento " + id + " no existe o ya no admite apuestas");
    }
}

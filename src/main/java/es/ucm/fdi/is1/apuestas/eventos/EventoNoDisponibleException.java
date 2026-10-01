package es.ucm.fdi.is1.apuestas.eventos;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EventoNoDisponibleException extends RuntimeException {

    public EventoNoDisponibleException(Long id) {
        super("El evento " + id + " no existe o ya no admite apuestas");
    }
}

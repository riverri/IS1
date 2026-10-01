package es.ucm.fdi.is1.apuestas.apuesta;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ApuestaNoEncontradaException extends RuntimeException {

    public ApuestaNoEncontradaException(Long id) {
        super("No existe la apuesta " + id);
    }
}

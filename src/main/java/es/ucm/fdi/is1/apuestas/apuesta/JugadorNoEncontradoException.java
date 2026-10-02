package es.ucm.fdi.is1.apuestas.apuesta;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class JugadorNoEncontradoException extends RuntimeException {

    public JugadorNoEncontradoException(Long id) {
        super("El jugador " + id + " no existe");
    }
}

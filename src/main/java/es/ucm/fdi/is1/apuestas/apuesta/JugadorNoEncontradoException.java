package es.ucm.fdi.is1.apuestas.apuesta;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * El perfil pedido no es de un jugador del ranking (HU-48): no existe, es el creador o la cuenta se eliminó.
 * Spring responde con un 404 gracias a {@code @ResponseStatus}.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class JugadorNoEncontradoException extends RuntimeException {

    public JugadorNoEncontradoException(Long id) {
        super("El jugador " + id + " no existe");
    }
}

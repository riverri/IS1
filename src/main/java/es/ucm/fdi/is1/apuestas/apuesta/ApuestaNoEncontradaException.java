package es.ucm.fdi.is1.apuestas.apuesta;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * La apuesta no existe o no es del usuario que la pide (cancelar o cambiar el importe, HU-26, HU-27).
 * Así no se revela si existe una apuesta ajena. Si nadie la captura, Spring responde con un 404
 * gracias a {@code @ResponseStatus}.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ApuestaNoEncontradaException extends RuntimeException {

    public ApuestaNoEncontradaException(Long id) {
        super("No existe la apuesta " + id);
    }
}

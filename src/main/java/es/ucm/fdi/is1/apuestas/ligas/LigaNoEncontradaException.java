package es.ucm.fdi.is1.apuestas.ligas;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** La liga no existe o el usuario no es miembro: en los dos casos se trata como inexistente. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LigaNoEncontradaException extends RuntimeException {

    public LigaNoEncontradaException(Long id) {
        super("No se ha encontrado la liga " + id);
    }
}

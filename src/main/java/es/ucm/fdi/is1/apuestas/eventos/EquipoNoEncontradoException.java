package es.ucm.fdi.is1.apuestas.eventos;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * No existe ningún equipo con ese identificador. La lanzan {@link FichaEquipoService} y la gestión de
 * equipos; gracias a {@code @ResponseStatus}, Spring responde con un 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EquipoNoEncontradoException extends RuntimeException {

    public EquipoNoEncontradoException(Long id) {
        super("El equipo " + id + " no existe");
    }
}

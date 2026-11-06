package es.ucm.fdi.is1.apuestas.anuncios;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** El anuncio pedido en Gestión no existe. Spring responde con un 404 gracias a {@code @ResponseStatus}. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AnuncioNoEncontradoException extends RuntimeException {

    public AnuncioNoEncontradoException(Long id) {
        super("No existe el anuncio " + id);
    }
}

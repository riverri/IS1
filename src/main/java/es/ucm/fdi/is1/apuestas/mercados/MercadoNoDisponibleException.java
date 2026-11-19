package es.ucm.fdi.is1.apuestas.mercados;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * El mercado no existe, no tiene ese candidato o ya no admite apuestas. La lanzan {@link MercadoService}
 * y {@code ApuestaService} al apostar (CA-44.3). El mensaje es el mismo en los tres casos.
 * Si ningún controlador la captura (por ejemplo, un mercado que no existe en el panel del creador),
 * Spring responde con un 404 gracias a {@code @ResponseStatus}, y no con un error 500.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MercadoNoDisponibleException extends RuntimeException {

    public MercadoNoDisponibleException(Long id) {
        super("Este mercado ya no admite apuestas");
    }
}

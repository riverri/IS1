package es.ucm.fdi.is1.apuestas.mercados;

/**
 * El mercado no existe, no tiene ese candidato o ya no admite apuestas. La lanzan {@link MercadoService}
 * y {@code ApuestaService} al apostar (CA-44.3). El mensaje es el mismo en los tres casos.
 */
public class MercadoNoDisponibleException extends RuntimeException {

    public MercadoNoDisponibleException(Long id) {
        super("Este mercado ya no admite apuestas");
    }
}

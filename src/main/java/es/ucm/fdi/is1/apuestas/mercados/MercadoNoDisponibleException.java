package es.ucm.fdi.is1.apuestas.mercados;

/** El mercado no existe, no tiene ese candidato o ya no admite apuestas. */
public class MercadoNoDisponibleException extends RuntimeException {

    public MercadoNoDisponibleException(Long id) {
        super("Este mercado ya no admite apuestas");
    }
}

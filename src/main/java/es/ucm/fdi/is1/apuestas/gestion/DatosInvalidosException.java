package es.ucm.fdi.is1.apuestas.gestion;

/**
 * Regla de negocio incumplida en un formulario; indica el campo afectado.
 * La lanza {@link GestionService} y el controlador la convierte en un error de ese campo, para mostrarlo
 * junto a él en lugar de en una página de error.
 */
public class DatosInvalidosException extends RuntimeException {

    private final String campo;

    public DatosInvalidosException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}

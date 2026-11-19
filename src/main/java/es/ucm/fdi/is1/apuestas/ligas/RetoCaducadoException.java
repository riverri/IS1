package es.ucm.fdi.is1.apuestas.ligas;

/**
 * Se ha intentado aceptar un reto cuyo partido ya no admite apuestas (HU-89): el reto se anula y se devuelve el
 * importe. {@link RetoService#aceptar} no deshace la transacción con esta excepción, para que la devolución se guarde.
 */
public class RetoCaducadoException extends IllegalStateException {

    public RetoCaducadoException() {
        super("El partido ya ha empezado: el reto se ha anulado y se ha devuelto el importe");
    }
}

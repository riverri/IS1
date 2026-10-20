package es.ucm.fdi.is1.apuestas.usuarios;

/**
 * Ya hay una cuenta con ese email, o usa el dominio reservado de las cuentas eliminadas (HU-11, CA-11.2).
 * La lanza {@link UsuarioService#crear}.
 */
public class EmailYaRegistradoException extends RuntimeException {

    public EmailYaRegistradoException(String email) {
        super("Ya existe una cuenta con el email " + email);
    }
}

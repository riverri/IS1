package es.ucm.fdi.is1.apuestas.usuarios;

public class EmailYaRegistradoException extends RuntimeException {

    public EmailYaRegistradoException(String email) {
        super("Ya existe una cuenta con el email " + email);
    }
}

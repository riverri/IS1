package es.ucm.fdi.is1.apuestas.apuesta;

/**
 * Manda el correo de una apuesta ganada (HU-77). Interfaz para poder cambiarla en las pruebas, como
 * {@code usuarios.EnvioCorreo}; la de la aplicación es {@link CorreoPremio}.
 */
public interface EnvioPremio {

    /** @param enlace dirección completa de <i>Mis apuestas</i> */
    void enviarPremio(String email, String nombre, String texto, String enlace);
}

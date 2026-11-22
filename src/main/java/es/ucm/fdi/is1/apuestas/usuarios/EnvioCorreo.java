package es.ucm.fdi.is1.apuestas.usuarios;

/**
 * Envía el enlace para recuperar la contraseña (HU-17). Interfaz para poder cambiarla en las pruebas.
 * La implementación de la aplicación es {@link CorreoRecuperacion}.
 *
 * <p>Una interfaz solo dice <i>qué</i> se puede hacer (el método, sin cuerpo); las clases que la implementan dicen
 * <i>cómo</i>. {@link RecuperacionService} pide un {@code EnvioCorreo} sin saber cuál le toca: en la aplicación
 * Spring le da {@link CorreoRecuperacion}, y en las pruebas se le da uno falso que no manda correos de verdad.
 */
public interface EnvioCorreo {

    /** @param enlace dirección completa, con el código, para crear la contraseña nueva */
    void enviarRecuperacion(String email, String nombre, String enlace);

    default void enviarVerificacion(String email, String nombre, String enlace) {
        throw new UnsupportedOperationException("Email verification is not configured");
    }

    default void notificarCambioSolicitado(String email, String nombre, String nuevoEmail) {
        throw new UnsupportedOperationException("Email change notifications are not configured");
    }

    default void enviarConfirmacionCambio(String email, String nombre, String enlace) {
        throw new UnsupportedOperationException("Email change confirmation is not configured");
    }
}

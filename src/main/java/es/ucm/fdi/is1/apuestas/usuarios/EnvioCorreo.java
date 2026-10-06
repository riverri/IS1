package es.ucm.fdi.is1.apuestas.usuarios;

/** Envía el enlace para recuperar la contraseña (HU-17). Interfaz para poder cambiarla en las pruebas. */
public interface EnvioCorreo {

    void enviarRecuperacion(String email, String nombre, String enlace);
}

package es.ucm.fdi.is1.apuestas.apuesta;

/** Manda el resumen semanal (HU-92). Interfaz para poder cambiarla en las pruebas; la de la aplicación es {@link CorreoResumen}. */
public interface EnvioResumen {

    void enviarResumen(String email, String nombre, String texto);
}

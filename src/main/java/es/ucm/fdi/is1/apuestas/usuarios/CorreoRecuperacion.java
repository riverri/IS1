package es.ucm.fdi.is1.apuestas.usuarios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Envía el enlace por correo si hay un servidor configurado (spring.mail.host, ver README). Si no lo hay,
 * por ejemplo en el ordenador de cada uno, el enlace se escribe en la consola de la aplicación.
 */
@Component
public class CorreoRecuperacion implements EnvioCorreo {

    private static final Logger LOG = LoggerFactory.getLogger(CorreoRecuperacion.class);

    private final ObjectProvider<JavaMailSender> correo;
    private final String servidorCorreo;
    private final String remitente;

    public CorreoRecuperacion(ObjectProvider<JavaMailSender> correo,
                              @Value("${spring.mail.host:}") String servidorCorreo,
                              @Value("${apuestas.correo.remitente:no-responder@apuestas.es}") String remitente) {
        this.correo = correo;
        this.servidorCorreo = servidorCorreo;
        this.remitente = remitente;
    }

    @Override
    public void enviarRecuperacion(String email, String nombre, String enlace) {
        JavaMailSender servidor = servidorCorreo.isBlank() ? null : correo.getIfAvailable();
        if (servidor == null) {
            LOG.info("No hay servidor de correo configurado. Enlace para recuperar la contraseña de {}: {}", email, enlace);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Recupera tu contraseña");
        mensaje.setText("Hola, " + nombre + ":\n\n"
                + "Para crear una contraseña nueva, abre este enlace (caduca en "
                + RecuperacionService.VALIDEZ.toMinutes() + " minutos y solo sirve una vez):\n\n"
                + enlace + "\n\n"
                + "Si no lo has pedido tú, ignora este correo: tu contraseña no cambia.\n");
        try {
            servidor.send(mensaje);
        } catch (MailException e) {
            // No se dice al usuario si el email existe: el error solo queda en el registro
            LOG.warn("No se ha podido enviar el correo de recuperación a {}: {}", email, e.getMessage());
        }
    }
}

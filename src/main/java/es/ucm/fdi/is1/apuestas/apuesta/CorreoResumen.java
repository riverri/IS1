package es.ucm.fdi.is1.apuestas.apuesta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Manda el resumen semanal por correo o, sin servidor de correo, lo escribe en la consola (como {@link CorreoPremio}). */
@Component
public class CorreoResumen implements EnvioResumen {

    private static final Logger LOG = LoggerFactory.getLogger(CorreoResumen.class);

    private final ObjectProvider<JavaMailSender> correo;
    private final String servidorCorreo;
    private final String remitente;

    public CorreoResumen(ObjectProvider<JavaMailSender> correo, @Value("${spring.mail.host:}") String servidorCorreo,
                         @Value("${apuestas.correo.remitente:no-responder@apuestas.es}") String remitente) {
        this.correo = correo;
        this.servidorCorreo = servidorCorreo;
        this.remitente = remitente;
    }

    @Override
    public void enviarResumen(String email, String nombre, String texto) {
        JavaMailSender servidor = servidorCorreo.isBlank() ? null : correo.getIfAvailable();
        if (servidor == null) {
            LOG.info("No hay servidor de correo configurado. Resumen semanal para {}:\n{}", email, texto);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Tu semana en Better");
        mensaje.setText("Hola, " + nombre + ":\n\n" + texto
                + "\n\nSi no quieres recibir este resumen, desactívalo en Mi cuenta.\n");
        try {
            servidor.send(mensaje);
        } catch (MailException e) {
            LOG.warn("No se ha podido enviar el resumen semanal a {}: {}", email, e.getMessage());
        }
    }
}

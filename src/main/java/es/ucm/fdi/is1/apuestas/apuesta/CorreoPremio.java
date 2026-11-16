package es.ucm.fdi.is1.apuestas.apuesta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Manda el correo de una apuesta ganada si hay un servidor configurado (spring.mail.host); si no, lo escribe en la
 * consola. Funciona igual que {@code usuarios.CorreoRecuperacion} (HU-17).
 */
@Component
public class CorreoPremio implements EnvioPremio {

    private static final Logger LOG = LoggerFactory.getLogger(CorreoPremio.class);

    private final ObjectProvider<JavaMailSender> correo;
    private final String servidorCorreo;
    private final String remitente;

    public CorreoPremio(ObjectProvider<JavaMailSender> correo,
                        @Value("${spring.mail.host:}") String servidorCorreo,
                        @Value("${apuestas.correo.remitente:no-responder@apuestas.es}") String remitente) {
        this.correo = correo;
        this.servidorCorreo = servidorCorreo;
        this.remitente = remitente;
    }

    @Override
    public void enviarPremio(String email, String nombre, String texto, String enlace) {
        JavaMailSender servidor = servidorCorreo.isBlank() ? null : correo.getIfAvailable();
        if (servidor == null) {
            LOG.info("No hay servidor de correo configurado. Correo de premio para {}: {} {}", email, texto, enlace);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Has ganado una apuesta");
        mensaje.setText("Hola, " + nombre + ":\n\n" + texto + "\n\n"
                + "Puedes verla en Mis apuestas:\n" + enlace + "\n\n"
                + "Si no quieres recibir estos correos, desactívalos en Mi cuenta.\n");
        try {
            servidor.send(mensaje);
        } catch (MailException e) {
            // El premio ya está pagado; si el correo falla, queda el aviso de la web
            LOG.warn("No se ha podido enviar el correo de premio a {}: {}", email, e.getMessage());
        }
    }
}

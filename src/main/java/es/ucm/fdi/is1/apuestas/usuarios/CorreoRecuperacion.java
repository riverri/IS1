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
 * Implementación de {@link EnvioCorreo} que usa {@link RecuperacionService} (HU-17).
 */
@Component
public class CorreoRecuperacion implements EnvioCorreo {

    // Logger: escribe mensajes en la consola de la aplicación (mejor que System.out.println: tiene niveles
    // info/warn/error y fecha). Los {} del mensaje se sustituyen por los parámetros que siguen
    private static final Logger LOG = LoggerFactory.getLogger(CorreoRecuperacion.class);

    // ObjectProvider: Spring Boot solo crea el JavaMailSender si hay servidor de correo configurado
    private final ObjectProvider<JavaMailSender> correo;
    private final String servidorCorreo;
    private final String remitente;
    private final boolean mostrarEnlacesLocales;

    public CorreoRecuperacion(ObjectProvider<JavaMailSender> correo,
                              @Value("${spring.mail.host:}") String servidorCorreo,
                              @Value("${apuestas.correo.remitente:no-responder@apuestas.es}") String remitente,
                              @Value("${apuestas.correo.mostrar-enlaces-locales:true}") boolean mostrarEnlacesLocales) {
        this.correo = correo;
        this.servidorCorreo = servidorCorreo;
        this.remitente = remitente;
        this.mostrarEnlacesLocales = mostrarEnlacesLocales;
        if ((servidorCorreo == null || servidorCorreo.isBlank()) && !mostrarEnlacesLocales) {
            LOG.warn("No hay servidor de correo (MAIL_HOST): las cuentas nuevas no recibirán el enlace para activarse");
        }
    }

    /** Manda el correo con el enlace o, si no hay servidor, lo escribe en el registro de la aplicación. */
    @Override
    public void enviarRecuperacion(String email, String nombre, String enlace) {
        JavaMailSender servidor = servidor();
        if (servidor == null) {
            registrarEnlaceLocal("recuperar contraseña", email, enlace);
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

    @Override
    public void enviarVerificacion(String email, String nombre, String enlace) {
        JavaMailSender servidor = servidor();
        if (servidor == null) {
            registrarEnlaceLocal("verificar correo", email, enlace);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Verifica tu correo electrónico");
        mensaje.setText("Hola, " + nombre + ":\n\n"
                + "Para activar tu cuenta, confirma que puedes recibir mensajes en esta dirección abriendo este enlace.\n"
                + "El enlace caduca en 24 horas y solo sirve una vez:\n\n"
                + enlace + "\n\nSi no has creado esta cuenta, ignora este correo.\n");
        enviar(servidor, mensaje, email, "verificación");
    }

    @Override
    public void notificarCambioSolicitado(String email, String nombre, String nuevoEmail) {
        JavaMailSender servidor = servidor();
        if (servidor == null) {
            LOG.info("Email change requested for account {}", email);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Solicitud de cambio de correo");
        mensaje.setText("Hola, " + nombre + ":\n\n"
                + "Se ha solicitado cambiar el correo de acceso de tu cuenta a " + nuevoEmail + ".\n"
                + "El cambio no se aplicará hasta confirmar el nuevo correo. Si no has sido tú, cambia tu contraseña.\n");
        enviar(servidor, mensaje, email, "aviso de cambio de correo");
    }

    @Override
    public void enviarConfirmacionCambio(String email, String nombre, String enlace) {
        JavaMailSender servidor = servidor();
        if (servidor == null) {
            registrarEnlaceLocal("confirmar cambio de correo", email, enlace);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Confirma tu nuevo correo de acceso");
        mensaje.setText("Hola, " + nombre + ":\n\n"
                + "Para cambiar el correo de acceso de tu cuenta, confirma la nueva dirección con este enlace.\n"
                + "Caduca en 24 horas y solo sirve una vez:\n\n" + enlace
                + "\n\nSi no has solicitado este cambio, ignora este correo.\n");
        enviar(servidor, mensaje, email, "confirmación de cambio de correo");
    }

    private JavaMailSender servidor() {
        return servidorCorreo.isBlank() ? null : correo.getIfAvailable();
    }

    private void registrarEnlaceLocal(String tipo, String email, String enlace) {
        if (mostrarEnlacesLocales) {
            LOG.info("Local email link for {} to {}: {}", tipo, email, enlace);
        } else {
            LOG.warn("No mail server is configured; {} email could not be sent", tipo);
        }
    }

    private void enviar(JavaMailSender servidor, SimpleMailMessage mensaje, String email, String tipo) {
        try {
            servidor.send(mensaje);
        } catch (MailException e) {
            LOG.warn("Could not send {} email to {}: {}", tipo, email, e.getMessage());
        }
    }
}

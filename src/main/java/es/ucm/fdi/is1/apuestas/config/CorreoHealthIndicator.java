package es.ucm.fdi.is1.apuestas.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Reports SMTP readiness without exposing credentials or taking the web offline. */
@Component("correo")
@Profile("nube")
public class CorreoHealthIndicator implements HealthIndicator {

    private final ObjectProvider<JavaMailSender> servidor;
    private final String host;
    private final String puerto;
    private final String usuario;
    private final String password;
    private final String remitente;

    public CorreoHealthIndicator(ObjectProvider<JavaMailSender> servidor,
                                 @Value("${spring.mail.host:}") String host,
                                 @Value("${spring.mail.port:}") String puerto,
                                 @Value("${spring.mail.username:}") String usuario,
                                 @Value("${spring.mail.password:}") String password,
                                 @Value("${apuestas.correo.remitente:}") String remitente) {
        this.servidor = servidor;
        this.host = host;
        this.puerto = puerto;
        this.usuario = usuario;
        this.password = password;
        this.remitente = remitente;
    }

    @Override
    public Health health() {
        List<String> faltan = new ArrayList<>();
        if (enBlanco(host)) {
            faltan.add("MAIL_HOST");
        }
        if (enBlanco(puerto)) {
            faltan.add("MAIL_PORT");
        }
        if (enBlanco(usuario)) {
            faltan.add("MAIL_USERNAME");
        }
        if (enBlanco(password)) {
            faltan.add("MAIL_PASSWORD");
        }
        if (enBlanco(remitente)) {
            faltan.add("APUESTAS_CORREO_REMITENTE");
        }
        if (!faltan.isEmpty()) {
            return Health.down().withDetail("faltanVariables", String.join(", ", faltan)).build();
        }
        JavaMailSender cliente = servidor.getIfAvailable();
        if (cliente == null) {
            return Health.down().withDetail("motivo", "Spring no ha creado el cliente SMTP").build();
        }
        if (cliente instanceof JavaMailSenderImpl smtp) {
            try {
                // Solo abre la sesión SMTP; no envía ningún mensaje.
                smtp.testConnection();
            } catch (MailException | MessagingException e) {
                return Health.down().withDetail("motivo", "No se pudo autenticar o conectar con el servidor SMTP")
                        .build();
            }
        }
        return Health.up().withDetail("servidor", host).withDetail("puerto", puerto).build();
    }

    private static boolean enBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}

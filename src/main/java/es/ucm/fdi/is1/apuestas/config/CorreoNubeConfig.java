package es.ucm.fdi.is1.apuestas.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Fails fast when the public deployment cannot send account-verification mail. */
@Configuration
@Profile("nube")
public class CorreoNubeConfig {

    public CorreoNubeConfig(@Value("${spring.mail.host:}") String host,
                            @Value("${spring.mail.port:}") String port,
                            @Value("${spring.mail.username:}") String username,
                            @Value("${spring.mail.password:}") String password,
                            @Value("${apuestas.correo.remitente:}") String remitente) {
        List<String> faltan = new ArrayList<>();
        if (enBlanco(host)) faltan.add("MAIL_HOST");
        if (enBlanco(port)) faltan.add("MAIL_PORT");
        if (enBlanco(username)) faltan.add("MAIL_USERNAME");
        if (enBlanco(password)) faltan.add("MAIL_PASSWORD");
        if (enBlanco(remitente)) faltan.add("APUESTAS_CORREO_REMITENTE");
        if (!faltan.isEmpty()) {
            throw new IllegalStateException("Faltan variables SMTP en el despliegue: " + String.join(", ", faltan)
                    + ". Configúralas en Render antes de registrar usuarios.");
        }
    }

    private static boolean enBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}

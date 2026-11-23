package es.ucm.fdi.is1.apuestas.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.health.contributor.Health;
import org.springframework.mail.javamail.JavaMailSender;

class CorreoHealthIndicatorTest {

    @Test
    void marcaDownLaConfiguracionIncompletaSinExponerLaContrasena() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
        CorreoHealthIndicator indicador = new CorreoHealthIndicator(proveedor, "", "", "", "secreto", "");

        Health salud = indicador.health();

        assertThat(salud.getStatus().getCode()).isEqualTo("DOWN");
        assertThat(salud.getDetails()).containsEntry("faltanVariables",
                "MAIL_HOST, MAIL_PORT, MAIL_USERNAME, APUESTAS_CORREO_REMITENTE");
        assertThat(salud.getDetails()).doesNotContainValue("secreto");
    }

    @Test
    void marcaUpCuandoHayClienteYConfiguracion() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
        when(proveedor.getIfAvailable()).thenReturn(mock(JavaMailSender.class));
        CorreoHealthIndicator indicador = new CorreoHealthIndicator(proveedor, "smtp-relay.brevo.com", "2525",
                "login", "secreto", "verificado@example.com");

        assertThat(indicador.health().getStatus().getCode()).isEqualTo("UP");
    }
}

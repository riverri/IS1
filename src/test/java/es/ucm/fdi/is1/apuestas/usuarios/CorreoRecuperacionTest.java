package es.ucm.fdi.is1.apuestas.usuarios;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;

/** Ensures registration links are sent through the configured SMTP sender. */
class CorreoRecuperacionTest {

    @Test
    void enviaElEnlaceDeVerificacionAlRemitenteSolicitado() {
        JavaMailSender servidor = mock(JavaMailSender.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
        when(proveedor.getIfAvailable()).thenReturn(servidor);
        CorreoRecuperacion correo = new CorreoRecuperacion(proveedor, "smtp.example", "verificado@example.com", false);

        correo.enviarVerificacion("persona@example.com", "", "https://example.com/registro/confirmar?token=abc");

        ArgumentCaptor<SimpleMailMessage> mensaje = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(servidor).send(mensaje.capture());
        assertThat(mensaje.getValue().getFrom()).isEqualTo("verificado@example.com");
        assertThat(mensaje.getValue().getTo()).containsExactly("persona@example.com");
        assertThat(mensaje.getValue().getText()).contains("registro/confirmar?token=abc");
    }
}

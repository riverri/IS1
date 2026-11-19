package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/** HU-92: el resumen semanal sale por correo si hay servidor; si no, o si el envío falla, no rompe nada. */
class CorreoResumenTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
    private final JavaMailSender servidor = mock(JavaMailSender.class);

    @Test
    void conServidorDeCorreoLlegaUnCorreoConElResumen() {
        when(proveedor.getIfAvailable()).thenReturn(servidor);
        new CorreoResumen(proveedor, "smtp.ejemplo.es", "no-responder@better.es")
                .enviarResumen("ana@ucm.es", "Ana", "Has jugado 10,00 monedas.");

        ArgumentCaptor<SimpleMailMessage> correo = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(servidor).send(correo.capture());
        assertThat(correo.getValue().getTo()).containsExactly("ana@ucm.es");
        assertThat(correo.getValue().getFrom()).isEqualTo("no-responder@better.es");
        assertThat(correo.getValue().getSubject()).isEqualTo("Tu semana en Better");
        assertThat(correo.getValue().getText()).startsWith("Hola, Ana:").contains("Has jugado 10,00 monedas.")
                .contains("desactívalo en Mi cuenta");
    }

    @Test
    void sinServidorDeCorreoNoSeIntentaEnviar() {
        new CorreoResumen(proveedor, "", "no-responder@better.es").enviarResumen("ana@ucm.es", "Ana", "Resumen");
        verifyNoInteractions(proveedor);
        verify(servidor, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void siElEnvioFallaNoSePropagaElError() {
        when(proveedor.getIfAvailable()).thenReturn(servidor);
        doThrow(new MailSendException("servidor caído")).when(servidor).send(any(SimpleMailMessage.class));
        assertThatCode(() -> new CorreoResumen(proveedor, "smtp.ejemplo.es", "no-responder@better.es")
                .enviarResumen("ana@ucm.es", "Ana", "Resumen")).doesNotThrowAnyException();
    }
}

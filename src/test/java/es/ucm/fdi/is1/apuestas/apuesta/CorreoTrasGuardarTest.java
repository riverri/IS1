package es.ucm.fdi.is1.apuestas.apuesta;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * HU-77: el correo de premio sale cuando se guarda la transacción y no sale si se deshace. Sin @Transactional en
 * la prueba: cada caso abre y cierra la suya con TransactionTemplate y no toca la base de datos.
 */
@SpringBootTest(properties = "apuestas.url-publica=https://better.onrender.com/")
class CorreoTrasGuardarTest {

    private static final PremioGanado PREMIO = new PremioGanado("ana@ucm.es", "Ana", "¡Has acertado!");

    @Autowired
    private ApplicationEventPublisher eventos;

    @Autowired
    private TransactionTemplate transaccion;

    @MockitoBean
    private EnvioPremio envio;

    @Test
    void alGuardarSeMandaConElEnlaceAMisApuestas() {
        transaccion.executeWithoutResult(estado -> eventos.publishEvent(PREMIO));

        verify(envio).enviarPremio("ana@ucm.es", "Ana", "¡Has acertado!", "https://better.onrender.com/apuestas");
    }

    @Test
    void siLaTransaccionSeDeshaceNoSale() {
        transaccion.executeWithoutResult(estado -> {
            eventos.publishEvent(PREMIO);
            estado.setRollbackOnly();
        });

        verify(envio, never()).enviarPremio(any(), any(), any(), any());
    }
}

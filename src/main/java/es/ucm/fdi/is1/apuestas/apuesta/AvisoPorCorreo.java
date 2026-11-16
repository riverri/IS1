package es.ucm.fdi.is1.apuestas.apuesta;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Manda el correo de una apuesta ganada cuando el resultado ya está guardado (HU-77). Con AFTER_COMMIT, Spring
 * espera a que termine bien la transacción que resolvió la apuesta: si falla y se deshace, el premio no existe y
 * el correo no sale.
 */
@Component
public class AvisoPorCorreo {

    private final EnvioPremio envio;
    private final String urlPublica;

    public AvisoPorCorreo(EnvioPremio envio, @Value("${apuestas.url-publica}") String urlPublica) {
        this.envio = envio;
        this.urlPublica = urlPublica;
    }

    /** fallbackExecution: si alguien resuelve sin transacción, el correo sale igual. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alGanar(PremioGanado premio) {
        envio.enviarPremio(premio.email(), premio.nombre(), premio.texto(),
                urlPublica.replaceAll("/+$", "") + "/apuestas");
    }
}

package es.ucm.fdi.is1.apuestas.usuarios;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Límite de correos para recuperar la contraseña (HU-17): como mucho {@value #MAXIMO} por cuenta cada
 * {@link #VENTANA}. Sin él, cualquiera podría llenar de correos el buzón de otra persona pidiendo el enlace
 * con su email una y otra vez.
 *
 * <p>Se guarda en memoria, como {@link IntentosLogin}: al reiniciar la aplicación se empieza de cero,
 * que para esto basta.
 */
@Component
public class EnviosRecuperacion {

    static final int MAXIMO = 3;
    static final Duration VENTANA = Duration.ofMinutes(30);

    // email → momentos de los últimos envíos. Concurrente: pueden llegar varias peticiones a la vez
    private final Map<String, List<Instant>> envios = new ConcurrentHashMap<>();
    private final Clock reloj;

    public EnviosRecuperacion(Clock reloj) {
        this.reloj = reloj;
    }

    /**
     * Si se puede enviar otro enlace a esa cuenta; si se puede, lo apunta.
     *
     * @return false si ya se han enviado {@value #MAXIMO} en la última media hora
     */
    public boolean permitir(String email) {
        Instant ahora = reloj.instant();
        Instant desde = ahora.minus(VENTANA);
        // Array de un elemento: una lambda no puede cambiar una variable local, pero sí lo que hay dentro de un array
        boolean[] permitido = {false};
        // compute: calcula el nuevo valor de la clave a partir del anterior, sin que otra petición se cuele en medio
        envios.compute(clave(email), (k, anteriores) -> {
            List<Instant> recientes = new ArrayList<>();
            if (anteriores != null) {
                anteriores.stream().filter(t -> t.isAfter(desde)).forEach(recientes::add);
            }
            if (recientes.size() < MAXIMO) {
                recientes.add(ahora);
                permitido[0] = true;
            }
            return List.copyOf(recientes);
        });
        // Limpieza: se olvidan las cuentas sin envíos recientes para que el mapa no crezca sin fin
        envios.values().removeIf(lista -> lista.stream().noneMatch(t -> t.isAfter(desde)));
        return permitido[0];
    }

    /** Olvida los envíos de esa cuenta (lo usan las pruebas para empezar de cero). */
    public void olvidar(String email) {
        envios.remove(clave(email));
    }

    private static String clave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}

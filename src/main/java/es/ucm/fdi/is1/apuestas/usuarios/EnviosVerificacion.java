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

/** Per-address limit for verification email requests. */
@Component
public class EnviosVerificacion {

    static final int MAXIMO = 3;
    static final Duration VENTANA = Duration.ofMinutes(30);

    private final Map<String, List<Instant>> envios = new ConcurrentHashMap<>();
    private final Clock reloj;

    public EnviosVerificacion(Clock reloj) {
        this.reloj = reloj;
    }

    public boolean permitir(String email) {
        Instant ahora = reloj.instant();
        Instant desde = ahora.minus(VENTANA);
        boolean[] permitido = {false};
        envios.compute(clave(email), (clave, anteriores) -> {
            List<Instant> recientes = new ArrayList<>();
            if (anteriores != null) {
                anteriores.stream().filter(fecha -> fecha.isAfter(desde)).forEach(recientes::add);
            }
            if (recientes.size() < MAXIMO) {
                recientes.add(ahora);
                permitido[0] = true;
            }
            return List.copyOf(recientes);
        });
        envios.values().removeIf(fechas -> fechas.stream().noneMatch(fecha -> fecha.isAfter(desde)));
        return permitido[0];
    }

    public void olvidar(String email) {
        envios.remove(clave(email));
    }

    private static String clave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}

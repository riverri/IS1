package es.ucm.fdi.is1.apuestas;

import java.time.LocalDateTime;
import java.time.ZoneId;

/** "Ahora" en la zona de la aplicación: si las pruebas usaran la del sistema, en un servidor en UTC no cuadraría. */
public final class Hora {

    public static final ZoneId MADRID = ZoneId.of("Europe/Madrid");

    private Hora() {
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(MADRID);
    }
}

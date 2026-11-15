package es.ucm.fdi.is1.apuestas.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del keep-alive (application.properties, prefijo apuestas.keep-alive). Spring la rellena al
 * arrancar, como {@code cripto.CriptoProperties}. El intervalo lo lee {@link KeepAlive}.
 *
 * @param url dirección pública de la web (en Render, la de la variable RENDER_EXTERNAL_URL). Vacía: no se hace
 *            ninguna petición, que es lo que pasa en local y en las pruebas
 */
@ConfigurationProperties("apuestas.keep-alive")
public record KeepAliveProperties(String url) {

    /** Si hay una dirección a la que llamar. */
    public boolean activo() {
        return url != null && !url.isBlank();
    }
}

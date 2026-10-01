package es.ucm.fdi.is1.apuestas.apuesta;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Límites de apuesta (application.properties, prefijo apuestas.limites; HU-07).
 *
 * @param maxSelecciones número máximo de selecciones en una combinada
 */
@ConfigurationProperties("apuestas.limites")
public record LimitesProperties(int maxSelecciones) {
}

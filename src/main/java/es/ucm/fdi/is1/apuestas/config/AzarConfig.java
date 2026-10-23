package es.ucm.fdi.is1.apuestas.config;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fuente de azar inyectable, como el {@link RelojConfig reloj}: en la aplicación es {@link SecureRandom}, que no se
 * puede adivinar, y en las pruebas se sustituye por un generador con semilla fija para que siempre salga lo mismo.
 *
 * <p>La usan la rotación de los anuncios (HU-60) y los juegos del casino (HU-56, HU-57). El azar se decide siempre
 * en el servidor: el navegador nunca elige el número de la ruleta ni las cartas.
 */
@Configuration
public class AzarConfig {

    /** {@code RandomGenerator} es la interfaz de Java para cualquier generador de números aleatorios. */
    @Bean
    public RandomGenerator azar() {
        return new SecureRandom();
    }
}

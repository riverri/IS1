package es.ucm.fdi.is1.apuestas.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj inyectable: en las pruebas se puede sustituir por uno fijo. */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}

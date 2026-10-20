package es.ucm.fdi.is1.apuestas.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj inyectable: en las pruebas se puede sustituir por uno fijo.
 * Las fechas de los partidos se guardan en hora de Madrid, así que "ahora" también tiene que serlo,
 * aunque el servidor esté en otra zona (en Render va en UTC y se podía apostar con el partido empezado).
 */
@Configuration
public class RelojConfig {

    /**
     * Reloj del sistema en la zona de apuestas.zona-horaria (Europe/Madrid si no se indica).
     * Los servicios piden la fecha con {@code LocalDateTime.now(reloj)} en vez de {@code LocalDateTime.now()}.
     */
    @Bean
    public Clock reloj(@Value("${apuestas.zona-horaria:Europe/Madrid}") String zonaHoraria) {
        return Clock.system(ZoneId.of(zonaHoraria));
    }
}

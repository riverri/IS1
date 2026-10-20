package es.ucm.fdi.is1.apuestas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de Better, la web de apuestas con moneditas virtuales.
 * <ul>
 *   <li>{@code @SpringBootApplication}: busca los componentes (controladores, servicios, repositorios…) en este
 *       paquete y sus subpaquetes y configura Spring automáticamente.</li>
 *   <li>{@code @ConfigurationPropertiesScan}: carga los records de configuración, como
 *       {@link es.ucm.fdi.is1.apuestas.api.ApiProperties}.</li>
 *   <li>{@code @EnableScheduling}: activa las tareas programadas de
 *       {@link es.ucm.fdi.is1.apuestas.api.SincronizacionProgramada}.</li>
 * </ul>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ApuestasApplication {

    /** Arranca el servidor web embebido (puerto 8080, o el de la variable de entorno PORT). */
    public static void main(String[] args) {
        SpringApplication.run(ApuestasApplication.class, args);
    }
}

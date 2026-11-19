package es.ucm.fdi.is1.apuestas.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * GET /ping: responde "ok" sin consultar la base de datos ni pintar ninguna plantilla (HU-85). Lo visita
 * {@link KeepAlive} cada pocos minutos para que Render no apague la web por inactividad, y sirve igual para
 * un monitor externo como UptimeRobot. Es público: está en las rutas permitidas de {@code SeguridadConfig}.
 *
 * <p>Es una ruta funcional ({@link RouterFunction}) y no un {@code @RestController} a propósito: los
 * {@code @ModelAttribute} de los {@code @ControllerAdvice} (como {@link UsuarioActualAdvice}, que lee los límites
 * de la base de datos) se ejecutan antes de cualquier controlador, también los REST. Con la base de datos caída o
 * lenta, el ping fallaría y Render daría la web por muerta.
 */
@Configuration
public class PingController {

    @Bean
    public RouterFunction<ServerResponse> ping() {
        return RouterFunctions.route()
                .GET("/ping", peticion -> ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body("ok"))
                .build();
    }
}

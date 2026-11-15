package es.ucm.fdi.is1.apuestas.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /ping: responde "ok" sin consultar la base de datos ni pintar ninguna plantilla (HU-85). Lo visita
 * {@link KeepAlive} cada pocos minutos para que Render no apague la web por inactividad, y sirve igual para
 * un monitor externo como UptimeRobot. Es público: está en las rutas permitidas de {@code SeguridadConfig}.
 */
@RestController
public class PingController {

    @GetMapping(value = "/ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ping() {
        return "ok";
    }
}

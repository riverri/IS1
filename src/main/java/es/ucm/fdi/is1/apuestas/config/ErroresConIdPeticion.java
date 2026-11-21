package es.ucm.fdi.is1.apuestas.config;

import java.util.Map;

import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

/**
 * Datos que recibe la página de error: los de Spring Boot (código, ruta…) más el identificador de la petición
 * ({@link IdPeticion}). La página lo enseña para que quien avise de un error pueda decir cuál fue, y se encuentren
 * sus líneas en el registro del servidor.
 */
@Component
public class ErroresConIdPeticion extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest peticion, ErrorAttributeOptions opciones) {
        Map<String, Object> atributos = super.getErrorAttributes(peticion, opciones);
        Object id = peticion.getAttribute(IdPeticion.ATRIBUTO, RequestAttributes.SCOPE_REQUEST);
        if (id != null) {
            atributos.put("peticion", id);
        }
        return atributos;
    }
}

package es.ucm.fdi.is1.apuestas.usuarios;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Al iniciar sesión se aplica la recarga gratuita pendiente y se vuelve a la página que se pedía
 * (HU-14, CA-14.2). {@code SeguridadConfig} lo registra como lo que se hace tras un login correcto.
 */
@Component
public class RecargaAlEntrarHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UsuarioService usuarios;

    public RecargaAlEntrarHandler(UsuarioService usuarios) {
        this.usuarios = usuarios;
        // Si no se venía de ninguna página protegida, se va al catálogo
        setDefaultTargetUrl("/eventos");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        // authentication.getName() es el email con el que ha entrado
        usuarios.consultarConRecarga(authentication.getName());
        // super: ejecuta lo que hacía la clase padre (redirigir a la página que se pedía antes del login)
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

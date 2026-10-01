package es.ucm.fdi.is1.apuestas.usuarios;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Al iniciar sesión se aplica la recarga gratuita pendiente y se vuelve a la página que se pedía. */
@Component
public class RecargaAlEntrarHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UsuarioService usuarios;

    public RecargaAlEntrarHandler(UsuarioService usuarios) {
        this.usuarios = usuarios;
        setDefaultTargetUrl("/eventos");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        usuarios.consultarConRecarga(authentication.getName());
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

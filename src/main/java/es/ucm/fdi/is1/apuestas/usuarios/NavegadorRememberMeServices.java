package es.ucm.fdi.is1.apuestas.usuarios;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;

/** Persistent login service that removes only the browser token presented at logout. */
public class NavegadorRememberMeServices extends PersistentTokenBasedRememberMeServices {

    private final JdbcNavegadorTokenRepository tokens;

    public NavegadorRememberMeServices(String key, UserDetailsService users, JdbcNavegadorTokenRepository tokens) {
        super(key, users, tokens);
        this.tokens = tokens;
    }

    @Override
    public void logout(jakarta.servlet.http.HttpServletRequest request,
                       jakarta.servlet.http.HttpServletResponse response, Authentication authentication) {
        String cookie = extractRememberMeCookie(request);
        if (cookie != null) {
            try {
                String[] values = decodeCookie(cookie);
                if (values.length == 2) {
                    tokens.removeTokenForSeries(values[0]);
                }
            } catch (org.springframework.security.web.authentication.rememberme.InvalidCookieException ignored) {
                // An invalid browser cookie must not prevent logout or affect other remembered devices.
            }
        }
        cancelCookie(request, response);
    }
}

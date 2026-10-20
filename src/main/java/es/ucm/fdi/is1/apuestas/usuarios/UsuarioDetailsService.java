package es.ucm.fdi.is1.apuestas.usuarios;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security carga los usuarios por email para el inicio de sesión (HU-12).
 * Con lo que devuelve, Spring comprueba la contraseña escrita contra el resumen BCrypt guardado;
 * nuestro código nunca compara contraseñas en el login.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioService usuarios;
    private final IntentosLogin intentos;

    public UsuarioDetailsService(UsuarioService usuarios, IntentosLogin intentos) {
        this.usuarios = usuarios;
        this.intentos = intentos;
    }

    /**
     * Datos de la cuenta para Spring Security: email, resumen de la contraseña, rol y si puede entrar.
     *
     * @throws UsernameNotFoundException si no hay cuenta; Spring da el mismo error genérico que con
     *                                   una contraseña incorrecta (CA-12.2)
     */
    @Override
    public UserDetails loadUserByUsername(String email) {
        return usuarios.buscar(email)
                .map(u -> User.withUsername(u.getEmail())
                        .password(u.getPasswordHash())
                        .roles(u.getRol().name())
                        // Cuenta eliminada (HU-18) o con demasiados fallos seguidos: no entra aunque acierte
                        .disabled(u.isEliminado())
                        .accountLocked(intentos.bloqueado(email))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}

package es.ucm.fdi.is1.apuestas.usuarios;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Spring Security carga los usuarios por email para el inicio de sesión (HU-12). */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioService usuarios;
    private final IntentosLogin intentos;

    public UsuarioDetailsService(UsuarioService usuarios, IntentosLogin intentos) {
        this.usuarios = usuarios;
        this.intentos = intentos;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return usuarios.buscar(email)
                .map(u -> User.withUsername(u.getEmail())
                        .password(u.getPasswordHash())
                        .roles(u.getRol().name())
                        .disabled(u.isEliminado())
                        .accountLocked(intentos.bloqueado(email))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}

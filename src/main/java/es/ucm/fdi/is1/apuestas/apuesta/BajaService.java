package es.ucm.fdi.is1.apuestas.apuesta;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.CuentaEliminada;
import es.ucm.fdi.is1.apuestas.usuarios.PasswordIncorrectaException;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Eliminar la cuenta (HU-18). Se cancelan las apuestas que aún se pueden cancelar, se borran los avisos
 * y se anonimiza el usuario: el email y el nombre desaparecen, el saldo queda a cero y ya no se puede entrar.
 * La fila se conserva para que las apuestas ya hechas sigan cuadrando; desaparece del ranking.
 */
@Service
public class BajaService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarios;
    private final ApuestaRepository apuestas;
    private final NotificacionService notificaciones;
    private final Clock reloj;
    private final ApplicationEventPublisher eventos;

    public BajaService(UsuarioService usuarioService, UsuarioRepository usuarios, ApuestaRepository apuestas,
                       NotificacionService notificaciones, Clock reloj, ApplicationEventPublisher eventos) {
        this.usuarioService = usuarioService;
        this.usuarios = usuarios;
        this.apuestas = apuestas;
        this.notificaciones = notificaciones;
        this.reloj = reloj;
        this.eventos = eventos;
    }

    @Transactional
    public void eliminarCuenta(String email, String password) {
        if (!usuarioService.passwordCorrecta(email, password)) {
            throw new PasswordIncorrectaException();
        }
        Usuario usuario = usuarios.findByEmail(email.trim().toLowerCase(Locale.ROOT)).orElseThrow();
        if (usuario.isCreador()) {
            throw new IllegalStateException("La cuenta del creador de apuestas no se puede eliminar");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        apuestas.findByUsuarioAndEstadoOrderByFechaDesc(usuario, EstadoApuesta.ACTIVA).stream()
                .filter(a -> a.cancelable(ahora))
                .forEach(a -> a.cancelar(ahora));
        notificaciones.borrarDe(usuario);
        eventos.publishEvent(new CuentaEliminada(usuario.getId()));
        usuarioService.anonimizar(usuario);
    }
}

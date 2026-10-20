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
 *
 * <p>Está en este paquete y no en el de usuarios porque necesita cancelar apuestas; así el paquete de
 * usuarios no depende del de apuestas.
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

    /**
     * Elimina la cuenta tras confirmar la contraseña (CA-18.2). Todo en una transacción: si algo falla,
     * la cuenta queda como estaba.
     *
     * @throws PasswordIncorrectaException si la contraseña no es la del usuario
     * @throws IllegalStateException       si es la cuenta del creador de apuestas
     */
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
        // Las que aún se pueden cancelar devuelven el importe (que se pierde al poner el saldo a cero).
        // Las demás se resuelven igual más adelante, pero ya no se le abona nada (Usuario.ajustar)
        apuestas.findByUsuarioAndEstadoOrderByFechaDesc(usuario, EstadoApuesta.ACTIVA).stream()
                .filter(a -> a.cancelable(ahora))
                .forEach(a -> a.cancelar(ahora));
        notificaciones.borrarDe(usuario);
        // Antes de anonimizar, para que otros paquetes (por ejemplo, las ligas) limpien lo suyo
        eventos.publishEvent(new CuentaEliminada(usuario.getId()));
        usuarioService.anonimizar(usuario);
    }
}

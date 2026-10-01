package es.ucm.fdi.is1.apuestas.notificaciones;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/** Avisos dentro de la web (HU-37). */
@Service
public class NotificacionService {

    private final NotificacionRepository notificaciones;
    private final UsuarioRepository usuarios;
    private final Clock reloj;

    public NotificacionService(NotificacionRepository notificaciones, UsuarioRepository usuarios, Clock reloj) {
        this.notificaciones = notificaciones;
        this.usuarios = usuarios;
        this.reloj = reloj;
    }

    @Transactional
    public Notificacion avisar(Usuario usuario, String tipo, String texto) {
        return notificaciones.save(new Notificacion(usuario, tipo, texto, LocalDateTime.now(reloj)));
    }

    @Transactional(readOnly = true)
    public long sinLeer(String email) {
        return notificaciones.countByUsuarioEmailAndLeidaFalse(email);
    }

    /** Las últimas notificaciones del usuario; las que aún no había leído salen marcadas como nuevas. */
    @Transactional
    public List<Aviso> leer(String email) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        List<Aviso> avisos = notificaciones.findTop50ByUsuarioOrderByFechaDescIdDesc(usuario).stream()
                .map(n -> new Aviso(n.getTipo(), n.getTexto(), n.getFecha(), !n.isLeida()))
                .toList();
        notificaciones.findByUsuarioAndLeidaFalse(usuario).forEach(Notificacion::marcarLeida);
        return avisos;
    }

    public record Aviso(String tipo, String texto, LocalDateTime fecha, boolean nuevo) {
    }
}

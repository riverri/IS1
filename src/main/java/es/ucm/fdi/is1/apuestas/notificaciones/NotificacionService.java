package es.ucm.fdi.is1.apuestas.notificaciones;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Servicio de los avisos dentro de la web (HU-37): crearlos, contarlos para la campana, leerlos y
 * borrarlos al eliminar la cuenta. No envía correos: los avisos solo se ven en la página de avisos.
 */
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

    /**
     * Guarda un aviso nuevo, sin leer, con la fecha de ahora.
     *
     * @param tipo para el icono de la vista: "ganada", "perdida", "anulada"…
     */
    @Transactional
    public Notificacion avisar(Usuario usuario, String tipo, String texto) {
        return notificaciones.save(new Notificacion(usuario, tipo, texto, LocalDateTime.now(reloj)));
    }

    /** Número de avisos sin leer, para la campana de la cabecera. */
    @Transactional(readOnly = true)
    public long sinLeer(String email) {
        return notificaciones.countByUsuarioEmailAndLeidaFalse(email);
    }

    /**
     * Las últimas 50 notificaciones del usuario, de la más reciente a la más antigua; las que aún no
     * había leído salen marcadas como nuevas y, a partir de ahora, quedan leídas. Se puede volver
     * a consultar un aviso ya leído (CA-37.3).
     */
    @Transactional
    public List<Aviso> leer(String email) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        List<Notificacion> mostradas = notificaciones.findTop50ByUsuarioOrderByFechaDescIdDesc(usuario);
        // Se copian antes de marcarlos: así la vista sabe cuáles eran nuevos
        List<Aviso> avisos = mostradas.stream()
                .map(n -> new Aviso(n.getTipo(), n.getTexto(), n.getFecha(), !n.isLeida()))
                .toList();
        // Solo se marcan como leídos los que se ven: los más antiguos siguen contando en la campana.
        // Sin save: dentro de la transacción, JPA guarda solo el cambio de "leida" de cada aviso
        mostradas.forEach(Notificacion::marcarLeida);
        return avisos;
    }

    /** Borra los avisos de un usuario que elimina su cuenta (HU-18). */
    @Transactional
    public void borrarDe(Usuario usuario) {
        notificaciones.deleteByUsuario(usuario);
    }

    /** Aviso tal como lo muestra la vista; {@code nuevo} si no se había leído antes de abrir la página. */
    public record Aviso(String tipo, String texto, LocalDateTime fecha, boolean nuevo) {
    }
}

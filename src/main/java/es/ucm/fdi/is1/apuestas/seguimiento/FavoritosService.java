package es.ucm.fdi.is1.apuestas.seguimiento;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoCreado;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Equipos favoritos (HU-86): marcarlos desde su ficha, ver sus próximos partidos los primeros en el catálogo y un
 * aviso cuando se publica un partido nuevo de alguno de ellos.
 */
@Service
public class FavoritosService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM 'a las' HH:mm");

    private final EquipoFavoritoRepository favoritos;
    private final EquipoRepository equipos;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final NotificacionService notificaciones;
    private final Clock reloj;

    public FavoritosService(EquipoFavoritoRepository favoritos, EquipoRepository equipos, EventoRepository eventos,
                            UsuarioRepository usuarios, NotificacionService notificaciones, Clock reloj) {
        this.favoritos = favoritos;
        this.equipos = equipos;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.notificaciones = notificaciones;
        this.reloj = reloj;
    }

    /**
     * Lo marca como favorito o, si ya lo era, lo quita (CA-86.1).
     *
     * @return si queda como favorito
     * @throws IllegalArgumentException si es el creador de apuestas, que no juega
     */
    @Transactional
    public boolean alternar(String email, Long equipoId) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        if (usuario.isCreador()) {
            throw new IllegalArgumentException("El creador de apuestas no sigue equipos");
        }
        // Un equipo que no existe responde 404, como su ficha (sin esto, error 500)
        Equipo equipo = equipos.findById(equipoId)
                .orElseThrow(() -> new es.ucm.fdi.is1.apuestas.eventos.EquipoNoEncontradoException(equipoId));
        return favoritos.findByUsuarioAndEquipo(usuario, equipo).map(f -> {
            favoritos.delete(f);
            return false;
        }).orElseGet(() -> {
            favoritos.save(new EquipoFavorito(usuario, equipo));
            return true;
        });
    }

    @Transactional(readOnly = true)
    public boolean esFavorito(String email, Long equipoId) {
        return usuarios.findByEmail(email)
                .flatMap(u -> equipos.findById(equipoId).flatMap(e -> favoritos.findByUsuarioAndEquipo(u, e)))
                .isPresent();
    }

    /** Ids de mis equipos favoritos. */
    @Transactional(readOnly = true)
    public Set<Long> misEquipos(String email) {
        Set<Long> ids = new LinkedHashSet<>();
        usuarios.findByEmail(email).ifPresent(u -> favoritos.findByUsuario(u).forEach(f -> ids.add(f.getEquipo().getId())));
        return ids;
    }

    /** Próximos partidos de mis equipos que admiten apuestas, por fecha (CA-86.2). */
    @Transactional(readOnly = true)
    public List<Evento> proximosPartidos(String email) {
        Set<Long> mios = misEquipos(email);
        if (mios.isEmpty()) {
            return List.of();
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return eventos.findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento.PROGRAMADO, ahora).stream()
                .filter(e -> mios.contains(e.getLocal().getId()) || mios.contains(e.getVisitante().getId()))
                .filter(e -> e.admiteApuestas(ahora))
                .limit(12)
                .toList();
    }

    /** Partido nuevo: aviso a quien sigue a alguno de los dos equipos, una vez aunque siga a los dos (CA-86.3). */
    @EventListener
    public void alCrearse(EventoCreado creado) {
        Evento evento = creado.evento();
        Set<Usuario> avisados = new LinkedHashSet<>();
        favoritos.findByEquipoIn(List.of(evento.getLocal(), evento.getVisitante()))
                .forEach(f -> avisados.add(f.getUsuario()));
        String texto = "Nuevo partido de tus equipos: " + evento.getLocal().getNombre() + " – "
                + evento.getVisitante().getNombre() + ", el " + evento.getFechaHora().format(FECHA) + ".";
        avisados.stream().filter(u -> !u.isEliminado()).forEach(u -> notificaciones.avisar(u, "favorito", texto));
    }
}

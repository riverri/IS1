package es.ucm.fdi.is1.apuestas.ligas;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.eventos.EventoBorrado;
import es.ucm.fdi.is1.apuestas.apuesta.JuegoResponsableService;
import es.ucm.fdi.is1.apuestas.apuesta.LimitesService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.eventos.EventoResuelto;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Retos 1 contra 1 entre compañeros de liga (HU-89). El retador elige partido, resultado e importe y paga; el retado
 * acepta eligiendo otro resultado y paga lo mismo, o lo rechaza. Al terminar el partido, el que acierta se lleva los
 * dos importes; si no acierta ninguno o se anula el partido, se devuelve a cada uno lo suyo.
 */
@Service
public class RetoService {

    private final RetoRepository retos;
    private final LigaRepository ligas;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final LimitesService limites;
    private final JuegoResponsableService juegoResponsable;
    private final NotificacionService notificaciones;
    private final Clock reloj;

    public RetoService(RetoRepository retos, LigaRepository ligas, EventoRepository eventos, UsuarioRepository usuarios,
                       LimitesService limites, JuegoResponsableService juegoResponsable,
                       NotificacionService notificaciones, Clock reloj) {
        this.retos = retos;
        this.ligas = ligas;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.limites = limites;
        this.juegoResponsable = juegoResponsable;
        this.notificaciones = notificaciones;
        this.reloj = reloj;
    }

    /**
     * Reta a un compañero de liga (CA-89.1): se cobra el importe al retador y el retado recibe un aviso.
     *
     * @throws IllegalArgumentException si no comparten liga, es él mismo, el pronóstico no vale o el importe no
     *                                  respeta los límites
     * @throws IllegalStateException    si el partido ya no admite apuestas
     */
    @Transactional
    public Reto retar(String email, Long retadoId, Long eventoId, Resultado pronostico, BigDecimal importe) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Usuario retador = usuarios.findByEmail(email).orElseThrow();
        Usuario retado = retadoId == null ? null : usuarios.findById(retadoId).orElse(null);
        if (retado == null || retado.getId().equals(retador.getId()) || retado.isEliminado()) {
            throw new IllegalArgumentException("Elige a un compañero de liga");
        }
        if (ligas.deMiembro(retador).stream().noneMatch(l -> l.esMiembro(retado))) {
            throw new IllegalArgumentException("Solo puedes retar a alguien de tus ligas");
        }
        Evento evento = eventoId == null ? null : eventos.findById(eventoId).orElse(null);
        if (evento == null || !evento.admiteApuestas(ahora)) {
            throw new IllegalStateException("Ese partido ya no admite apuestas");
        }
        comprobarPronostico(evento, pronostico);
        if (importe == null) {
            throw new IllegalArgumentException("Indica el importe");
        }
        limites.actuales().comprobarImporte(importe);
        juegoResponsable.comprobar(retador, importe, ahora);
        retador.cargar(importe, TipoMovimiento.RETO_APUESTA, "Reto a " + retado.getNombre() + ": " + titulo(evento), ahora);
        Reto reto = retos.save(new Reto(retador, retado, evento, pronostico, importe, ahora));
        notificaciones.avisar(retado, "info", retador.getNombre() + " te reta en " + titulo(evento) + ": "
                + monedas(importe) + " monedas cada uno a que sale «" + pronostico.getDescripcion() + "». Acepta o rechaza en tus ligas.");
        return reto;
    }

    /**
     * El retado acepta con otro resultado y paga lo mismo (CA-89.2).
     *
     * @throws IllegalArgumentException si no es el retado o elige el mismo resultado
     * @throws IllegalStateException    si ya no está pendiente
     * @throws RetoCaducadoException    si el partido ya no admite apuestas: el reto se anula y se devuelve el importe.
     *                                  No deshace la transacción, para que esa devolución se guarde
     */
    @Transactional(noRollbackFor = RetoCaducadoException.class)
    public Reto aceptar(String email, Long retoId, Resultado pronostico) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        Reto reto = pendiente(retoId);
        if (!reto.getRetado().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Este reto no es para ti");
        }
        if (!reto.getEvento().admiteApuestas(ahora)) {
            devolver(reto, ahora);
            throw new RetoCaducadoException();
        }
        comprobarPronostico(reto.getEvento(), pronostico);
        if (pronostico == reto.getPronosticoRetador()) {
            throw new IllegalArgumentException("Elige un resultado distinto del de " + reto.getRetador().getNombre());
        }
        juegoResponsable.comprobar(usuario, reto.getImporte(), ahora);
        usuario.cargar(reto.getImporte(), TipoMovimiento.RETO_APUESTA,
                "Reto de " + reto.getRetador().getNombre() + ": " + titulo(reto.getEvento()), ahora);
        reto.aceptar(pronostico);
        notificaciones.avisar(reto.getRetador(), "info", usuario.getNombre() + " ha aceptado tu reto en "
                + titulo(reto.getEvento()) + " con «" + pronostico.getDescripcion() + "».");
        return reto;
    }

    /** El retado lo rechaza o el retador lo retira: se devuelve el importe al retador (CA-89.2). */
    @Transactional
    public Reto rechazar(String email, Long retoId) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        Reto reto = pendiente(retoId);
        if (!reto.esDe(usuario)) {
            throw new IllegalArgumentException("Este reto no es tuyo");
        }
        reto.getRetador().abonar(reto.getImporte(), TipoMovimiento.RETO_DEVOLUCION,
                "Reto rechazado: " + titulo(reto.getEvento()), ahora);
        reto.cerrar(EstadoReto.RECHAZADO, null);
        notificaciones.avisar(reto.rival(usuario), "anulada", usuario.getNombre() + " ha "
                + (usuario.getId().equals(reto.getRetado().getId()) ? "rechazado tu reto" : "retirado su reto") + " en "
                + titulo(reto.getEvento()) + ".");
        return reto;
    }

    /** Mis retos, el más reciente primero. */
    @Transactional(readOnly = true)
    public List<Reto> mios(String email) {
        List<Reto> lista = retos.deUsuario(usuarios.findByEmail(email).orElseThrow());
        lista.forEach(r -> r.getEvento().getLocal().getNombre());
        return lista;
    }

    /** Partidos a los que se puede retar: los próximos que admiten apuestas. */
    @Transactional(readOnly = true)
    public List<Evento> partidosDisponibles() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return eventos.findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento.PROGRAMADO, ahora).stream()
                .filter(e -> e.admiteApuestas(ahora)).limit(40).toList();
    }

    /** Con retos hay dinero de por medio: el evento no se puede borrar, solo anular (y se devuelve). */
    @EventListener
    public void alBorrarse(EventoBorrado borrado) {
        if (retos.existsByEvento(borrado.evento())) {
            throw new IllegalStateException("El evento tiene retos 1 contra 1: no se puede borrar, solo anular "
                    + "(se devuelve el importe).");
        }
    }

    /** El partido ha terminado o se ha anulado: se resuelven sus retos (CA-89.3). */
    @EventListener
    public void alResolverse(EventoResuelto resuelto) {
        Evento evento = resuelto.evento();
        LocalDateTime ahora = LocalDateTime.now(reloj);
        for (Reto reto : retos.findByEventoAndEstadoIn(evento, EnumSet.of(EstadoReto.PENDIENTE, EstadoReto.ACEPTADO))) {
            Resultado r = evento.getResultado();
            if (reto.getEstado() == EstadoReto.ACEPTADO && evento.getEstado() == EstadoEvento.FINALIZADO
                    && (r == reto.getPronosticoRetador() || r == reto.getPronosticoRetado())) {
                Usuario gana = r == reto.getPronosticoRetador() ? reto.getRetador() : reto.getRetado();
                gana.abonar(reto.getBote(), TipoMovimiento.RETO_PREMIO, "Reto ganado: " + titulo(evento), ahora);
                reto.cerrar(EstadoReto.RESUELTO, gana);
                notificaciones.avisar(gana, "ganada", "¡Has ganado el reto contra " + reto.rival(gana).getNombre()
                        + " en " + titulo(evento) + "! Cobras " + monedas(reto.getBote()) + " monedas.");
                notificaciones.avisar(reto.rival(gana), "perdida", "Has perdido el reto contra " + gana.getNombre()
                        + " en " + titulo(evento) + ".");
            } else {
                devolver(reto, ahora);
            }
        }
    }

    /** Devuelve a cada uno lo que puso (al retador solo, si aún no se había aceptado). */
    private void devolver(Reto reto, LocalDateTime ahora) {
        String concepto = "Reto devuelto: " + titulo(reto.getEvento());
        reto.getRetador().abonar(reto.getImporte(), TipoMovimiento.RETO_DEVOLUCION, concepto, ahora);
        notificaciones.avisar(reto.getRetador(), "anulada", "Se ha devuelto tu reto en " + titulo(reto.getEvento())
                + ": " + monedas(reto.getImporte()) + " monedas.");
        if (reto.getEstado() == EstadoReto.ACEPTADO) {
            reto.getRetado().abonar(reto.getImporte(), TipoMovimiento.RETO_DEVOLUCION, concepto, ahora);
            notificaciones.avisar(reto.getRetado(), "anulada", "Se ha devuelto tu reto en " + titulo(reto.getEvento())
                    + ": " + monedas(reto.getImporte()) + " monedas.");
        }
        reto.cerrar(EstadoReto.DEVUELTO, null);
    }

    private Reto pendiente(Long retoId) {
        Reto reto = retos.findById(retoId).orElseThrow(() -> new IllegalArgumentException("No existe ese reto"));
        if (reto.getEstado() != EstadoReto.PENDIENTE) {
            throw new IllegalStateException("Este reto ya no está pendiente");
        }
        return reto;
    }

    private static void comprobarPronostico(Evento evento, Resultado pronostico) {
        if (pronostico == null || (pronostico == Resultado.EMPATE && !evento.getDeporte().isAdmiteEmpate())) {
            throw new IllegalArgumentException("Elige un resultado que pueda salir en ese partido");
        }
    }

    private static String titulo(Evento evento) {
        return evento.getLocal().getNombre() + " – " + evento.getVisitante().getNombre();
    }

    private static String monedas(BigDecimal cantidad) {
        return cantidad.setScale(2).toPlainString().replace('.', ',');
    }
}

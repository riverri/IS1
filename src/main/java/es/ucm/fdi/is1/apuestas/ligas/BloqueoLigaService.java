package es.ucm.fdi.is1.apuestas.ligas;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.JuegoResponsableService;
import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoBorrado;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.eventos.EventoResuelto;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Bloqueos de un compañero por 50 monedas (HU-105) o de toda la liga por 100 (HU-106).
 *
 * <p>Lo que se paga es dinero que gana la casa (sale en el beneficio diario, HU-95) y cuenta como jugado en el juego
 * responsable (HU-10): con la cuenta en pausa o pasado el límite no se puede bloquear. Si el partido se anula o se
 * borra, se devuelve el coste a quien lo pagó y el bloqueo desaparece; también si se borra la liga, mientras su partido
 * no haya empezado. Si solo se suspende, el bloqueo sigue: vale para cuando se juegue.
 *
 * <p>Un doble clic no cobra dos veces: los dos envíos cambian el saldo del mismo usuario, que tiene control de
 * versión ({@code @Version}), así que el segundo falla al guardar y se deshace entero, también su bloqueo.
 */
@Service
public class BloqueoLigaService {
    public static final BigDecimal COSTE = new BigDecimal("50.00");
    /** Bloquear a toda la liga (HU-106). */
    public static final BigDecimal COSTE_LIGA = new BigDecimal("100.00");
    private final BloqueoLigaRepository bloqueos;
    private final LigaRepository ligas;
    private final UsuarioRepository usuarios;
    private final EventoRepository eventos;
    private final JuegoResponsableService juegoResponsable;
    private final Clock reloj;

    public BloqueoLigaService(BloqueoLigaRepository bloqueos, LigaRepository ligas, UsuarioRepository usuarios,
                              EventoRepository eventos, JuegoResponsableService juegoResponsable, Clock reloj) {
        this.bloqueos = bloqueos;
        this.ligas = ligas;
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.juegoResponsable = juegoResponsable;
        this.reloj = reloj;
    }

    @Transactional
    public BloqueoLiga crear(Long ligaId, String email, Long objetivoId, Long eventoId, String alcance) {
        Usuario creador = usuario(email);
        Liga liga = ligas.findById(ligaId).filter(l -> l.esMiembro(creador))
                .orElseThrow(() -> new LigaNoEncontradaException(ligaId));
        Usuario objetivo = usuarios.findById(objetivoId)
                .filter(u -> liga.esMiembro(u) && !u.getId().equals(creador.getId()) && !u.isEliminado())
                .orElseThrow(() -> new IllegalArgumentException("Elige a un compañero de esta liga"));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Evento evento = eventos.paraApostar(eventoId).filter(e -> e.admiteApuestas(ahora))
                .orElseThrow(() -> new IllegalArgumentException("Solo puedes bloquear un partido futuro y programado"));
        if (evento.getEstado() != EstadoEvento.PROGRAMADO || !evento.getFechaHora().isAfter(ahora)) {
            throw new IllegalArgumentException("Solo puedes bloquear un partido futuro y programado");
        }
        String seleccionado = normalizarAlcance(alcance, evento);
        if (bloqueos.existsByLigaAndObjetivoAndEventoAndAlcance(liga, objetivo, evento, seleccionado)) {
            throw new IllegalArgumentException("Ese bloqueo ya está activo");
        }
        juegoResponsable.comprobar(creador, COSTE, ahora);
        creador.cargar(COSTE, TipoMovimiento.BLOQUEO_LIGA,
                "Bloqueo para " + objetivo.getNombre() + " en " + evento.getLocal().getNombre()
                        + " – " + evento.getVisitante().getNombre(), ahora);
        return bloqueos.save(new BloqueoLiga(liga, creador, objetivo, evento, seleccionado, ahora));
    }

    /** Paga 100 monedas para bloquear el mismo alcance a todos los miembros salvo quien lo activa (HU-106). */
    @Transactional
    public BloqueoLiga crearParaLiga(Long ligaId, String email, Long eventoId, String alcance) {
        Usuario creador = usuario(email);
        Liga liga = ligas.findById(ligaId).filter(l -> l.esMiembro(creador))
                .orElseThrow(() -> new LigaNoEncontradaException(ligaId));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Evento evento = eventos.paraApostar(eventoId).filter(e -> e.admiteApuestas(ahora))
                .orElseThrow(() -> new IllegalArgumentException("Solo puedes bloquear un partido futuro y programado"));
        String seleccionado = normalizarAlcance(alcance, evento);
        if (bloqueos.existsByLigaAndEventoAndAlcanceAndObjetivoIsNull(liga, evento, seleccionado)) {
            throw new IllegalArgumentException("Ya hay un bloqueo activo para toda la liga en ese alcance");
        }
        juegoResponsable.comprobar(creador, COSTE_LIGA, ahora);
        creador.cargar(COSTE_LIGA, TipoMovimiento.BLOQUEO_LIGA,
                "Bloqueo para toda la liga en " + evento.getLocal().getNombre() + " – "
                        + evento.getVisitante().getNombre(), ahora);
        return bloqueos.save(new BloqueoLiga(liga, creador, null, evento, seleccionado, ahora));
    }

    /** Partido anulado: se devuelve lo que costó cada bloqueo. Con un resultado normal no se toca nada. */
    @EventListener
    public void alResolverse(EventoResuelto resuelto) {
        if (resuelto.evento().getEstado() == EstadoEvento.ANULADO) {
            devolver(resuelto.evento(), "partido anulado");
        }
    }

    /** Partido borrado por el creador de apuestas (antes de empezar): igual, y así el evento se puede borrar. */
    @EventListener
    public void alBorrarse(EventoBorrado borrado) {
        devolver(borrado.evento(), "partido borrado");
    }

    /**
     * Liga borrada (por quien la creó, o al eliminar su cuenta): sus bloqueos dejan de valer. Se devuelven los que aún
     * no han llegado a su partido (los ya jugados sí sirvieron) y se borran todos; si no, la liga no se podría borrar.
     */
    @EventListener
    public void alBorrarseLiga(LigaBorrada borrada) {
        List<BloqueoLiga> deLaLiga = bloqueos.findByLiga(borrada.liga());
        LocalDateTime ahora = LocalDateTime.now(reloj);
        for (BloqueoLiga bloqueo : deLaLiga) {
            if (bloqueo.activa(ahora)) {
                devolverCoste(bloqueo, "liga borrada", ahora);
            }
        }
        bloqueos.deleteAll(deLaLiga);
    }

    private void devolver(Evento evento, String motivo) {
        List<BloqueoLiga> delEvento = bloqueos.findByEvento(evento);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        for (BloqueoLiga bloqueo : delEvento) {
            devolverCoste(bloqueo, motivo, ahora);
        }
        // Se borra: no puede devolverse dos veces, y un partido anulado o borrado ya no admite apuestas que bloquear
        bloqueos.deleteAll(delEvento);
    }

    private static void devolverCoste(BloqueoLiga bloqueo, String motivo, LocalDateTime ahora) {
        BigDecimal coste = bloqueo.getObjetivo() == null ? COSTE_LIGA : COSTE;
        Evento evento = bloqueo.getEvento();
        // abonar no hace nada si la cuenta ya está eliminada
        bloqueo.getCreador().abonar(coste, TipoMovimiento.DEVOLUCION, "Devolución del bloqueo (" + motivo + "): "
                + evento.getLocal().getNombre() + " – " + evento.getVisitante().getNombre(), ahora);
    }

    /** Comprueba la regla al apostar; no se confía en que la interfaz oculte la selección. */
    @Transactional(readOnly = true)
    public void comprobar(Usuario apostador, Evento evento, Resultado resultado, Especial especial) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        for (BloqueoLiga bloqueo : bloqueos.findByEvento(evento)) {
            boolean destinatario = bloqueo.getObjetivo() == null
                    ? !bloqueo.getCreador().getId().equals(apostador.getId())
                    : bloqueo.getObjetivo().getId().equals(apostador.getId());
            if (!destinatario || !bloqueo.activa(ahora) || !bloqueo.getLiga().esMiembro(apostador)) {
                continue;
            }
            String alcance = bloqueo.getAlcance();
            if (alcance.equals("PARTIDO") || alcance.equals(resultado == null ? "" : resultado.name())
                    || alcance.equals(especial == null ? "" : especial.name())) {
                throw new IllegalArgumentException("No puedes apostar a esta selección: está bloqueada por un compañero de tu liga");
            }
        }
    }

    @Transactional(readOnly = true)
    public List<BloqueoLiga> deLiga(Liga liga) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return bloqueos.activosDeLiga(liga, ahora);
    }

    private Usuario usuario(String email) {
        return usuarios.findByEmail(email).orElseThrow();
    }

    private static String normalizarAlcance(String alcance, Evento evento) {
        if ("PARTIDO".equals(alcance)) {
            return alcance;
        }
        try {
            Resultado resultado = Resultado.valueOf(alcance);
            if (resultado != Resultado.EMPATE || evento.getDeporte().isAdmiteEmpate()) {
                return resultado.name();
            }
        } catch (RuntimeException ignorada) { }
        try {
            Especial especial = Especial.valueOf(alcance);
            if (evento.getDeporte() == Deporte.FUTBOL) {
                return especial.name();
            }
        } catch (RuntimeException ignorada) { }
        throw new IllegalArgumentException("Elige un partido completo o una selección válida");
    }
}

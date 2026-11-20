package es.ucm.fdi.is1.apuestas.ligas;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/** Bloqueos de un compañero por 50 monedas (HU-105). */
@Service
public class BloqueoLigaService {
    public static final BigDecimal COSTE = new BigDecimal("50.00");
    private final BloqueoLigaRepository bloqueos;
    private final LigaRepository ligas;
    private final UsuarioRepository usuarios;
    private final EventoRepository eventos;
    private final Clock reloj;

    public BloqueoLigaService(BloqueoLigaRepository bloqueos, LigaRepository ligas, UsuarioRepository usuarios,
                              EventoRepository eventos, Clock reloj) {
        this.bloqueos = bloqueos;
        this.ligas = ligas;
        this.usuarios = usuarios;
        this.eventos = eventos;
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
        creador.cargar(new BigDecimal("100.00"), TipoMovimiento.BLOQUEO_LIGA,
                "Bloqueo para toda la liga en " + evento.getLocal().getNombre() + " – "
                        + evento.getVisitante().getNombre(), ahora);
        return bloqueos.save(new BloqueoLiga(liga, creador, null, evento, seleccionado, ahora));
    }

    /** Comprueba la regla al apostar; no se confía en que la interfaz oculte la selección. */
    @Transactional(readOnly = true)
    public void comprobar(Usuario apostador, Evento evento, Resultado resultado, Especial especial) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        for (BloqueoLiga bloqueo : bloqueos.findByEvento(evento)) {
            boolean destinatario = bloqueo.getObjetivo() == null
                    ? !bloqueo.getCreador().getId().equals(apostador.getId())
                    : bloqueo.getObjetivo().getId().equals(apostador.getId());
            if (!destinatario || !bloqueo.activa(ahora) || !bloqueo.getLiga().esMiembro(apostador)) continue;
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
        if ("PARTIDO".equals(alcance)) return alcance;
        try {
            Resultado resultado = Resultado.valueOf(alcance);
            if (resultado != Resultado.EMPATE || evento.getDeporte().isAdmiteEmpate()) return resultado.name();
        } catch (RuntimeException ignorada) { }
        try {
            Especial especial = Especial.valueOf(alcance);
            if (evento.getDeporte() == Deporte.FUTBOL) return especial.name();
        } catch (RuntimeException ignorada) { }
        throw new IllegalArgumentException("Elige un partido completo o una selección válida");
    }
}

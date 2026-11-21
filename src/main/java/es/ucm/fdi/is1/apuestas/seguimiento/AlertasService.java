package es.ucm.fdi.is1.apuestas.seguimiento;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.SupercuotaService;
import es.ucm.fdi.is1.apuestas.cuotas.Cuotas;
import es.ucm.fdi.is1.apuestas.cuotas.CuotasCambiadas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoBorrado;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.CuentaEliminada;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Alertas de cuota (HU-90): avisan una vez cuando la cuota de un resultado llega al valor elegido. Se comprueban
 * cada vez que cambian las cuotas de un evento ({@link CuotasCambiadas}) y, por si cambian por otro motivo (la forma
 * de los equipos, la API), cada {@code apuestas.alertas.intervalo}.
 */
@Service
public class AlertasService {

    /** Alertas pendientes que puede tener un usuario en un mismo evento. */
    static final int MAXIMO_POR_EVENTO = 3;

    private final AlertaCuotaRepository alertas;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final SupercuotaService supercuotas;
    private final NotificacionService notificaciones;
    private final Clock reloj;

    public AlertasService(AlertaCuotaRepository alertas, EventoRepository eventos, UsuarioRepository usuarios,
                          SupercuotaService supercuotas,
                          NotificacionService notificaciones, Clock reloj) {
        this.alertas = alertas;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.supercuotas = supercuotas;
        this.notificaciones = notificaciones;
        this.reloj = reloj;
    }

    /**
     * Crea una alerta (CA-90.1).
     *
     * @throws IllegalArgumentException si la cuota no es mayor que la actual, el resultado no existe en el evento, ya
     *                                  tiene demasiadas o es el creador
     * @throws IllegalStateException    si el evento ya no admite apuestas
     */
    @Transactional
    public AlertaCuota crear(String email, Long eventoId, Resultado resultado, BigDecimal cuota) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        if (usuario.isCreador()) {
            throw new IllegalArgumentException("El creador de apuestas no pone alertas");
        }
        // Sin mensaje, un partido que no existe acabaría en un error 500 en vez de en el aviso de la página
        Evento evento = (eventoId == null ? Optional.<Evento>empty() : eventos.findById(eventoId))
                .orElseThrow(() -> new IllegalArgumentException("No existe ese partido"));
        if (!evento.admiteApuestas(ahora)) {
            throw new IllegalStateException("Este partido ya no admite apuestas");
        }
        // La cuota que se ve en la página: con supercuota (HU-87), la supercuota
        BigDecimal actual = resultado == null ? null : supercuotas.cuotasConSupercuota(evento).de(resultado);
        if (actual == null) {
            throw new IllegalArgumentException("Elige el resultado de la alerta");
        }
        if (cuota == null || cuota.compareTo(actual) <= 0 || cuota.stripTrailingZeros().scale() > 2
                || cuota.compareTo(new BigDecimal("1000")) > 0) {
            throw new IllegalArgumentException("La cuota de la alerta tiene que ser mayor que la actual ("
                    + texto(actual) + "), hasta 1.000 y con 2 decimales como mucho");
        }
        if (alertas.findByUsuarioAndEventoOrderByIdAsc(usuario, evento).stream().filter(AlertaCuota::isPendiente)
                .count() >= MAXIMO_POR_EVENTO) {
            throw new IllegalArgumentException("Como mucho " + MAXIMO_POR_EVENTO + " alertas pendientes por partido");
        }
        return alertas.save(new AlertaCuota(usuario, evento, resultado, cuota, ahora));
    }

    /** Mis alertas en un evento, las pendientes y las ya avisadas. */
    @Transactional(readOnly = true)
    public List<AlertaCuota> mias(String email, Evento evento) {
        return usuarios.findByEmail(email).map(u -> alertas.findByUsuarioAndEventoOrderByIdAsc(u, evento))
                .orElse(List.of());
    }

    /** Borra una alerta mía. @return el id del evento, para volver a su página */
    @Transactional
    public Long borrar(String email, Long alertaId) {
        AlertaCuota alerta = alertas.findById(alertaId).filter(a -> a.getUsuario().getEmail().equals(email))
                .orElseThrow(() -> new IllegalArgumentException("No existe esa alerta"));
        alertas.delete(alerta);
        return alerta.getEvento().getId();
    }

    /** Al eliminar una cuenta (HU-18) se borran sus alertas: si no, seguirían saltando y creando avisos. */
    @EventListener
    public void alEliminarCuenta(CuentaEliminada cuenta) {
        usuarios.findById(cuenta.usuarioId()).ifPresent(alertas::deleteByUsuario);
    }

    /** Al borrar un evento creado por error, sus alertas se van con él. */
    @EventListener
    public void alBorrarse(EventoBorrado borrado) {
        alertas.deleteByEvento(borrado.evento());
    }

    /** Han cambiado las cuotas de un evento: avisa a las alertas a las que se ha llegado (CA-90.2). */
    @EventListener
    @Transactional
    public void alCambiar(CuotasCambiadas cambio) {
        List<AlertaCuota> pendientes = alertas.findByEventoIdAndAvisadaIsNull(cambio.eventoId());
        if (!pendientes.isEmpty()) {
            // Las nuevas cuotas, con la supercuota si la hay: la misma que se ve en la página
            comprobar(pendientes, supercuotas.cuotasConSupercuota(pendientes.get(0).getEvento()), LocalDateTime.now(reloj));
        }
    }

    /**
     * Repaso de todas las alertas pendientes; las de partidos que ya no admiten apuestas se borran (CA-90.3). Las de
     * un partido suspendido se guardan sin comprobarlas: si se reactiva, vuelven a valer.
     */
    @Scheduled(initialDelayString = "${apuestas.alertas.intervalo:PT10M}", fixedDelayString = "${apuestas.alertas.intervalo:PT10M}")
    @Transactional
    public void repasar() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Map<Evento, List<AlertaCuota>> porEvento = new HashMap<>();
        alertas.findByAvisadaIsNull().forEach(a -> porEvento.computeIfAbsent(a.getEvento(), e -> new ArrayList<>()).add(a));
        porEvento.forEach((evento, lista) -> {
            if (evento.admiteApuestas(ahora)) {
                comprobar(lista, supercuotas.cuotasConSupercuota(evento), ahora);
            } else if (evento.getEstado() != EstadoEvento.SUSPENDIDO) {
                // Suspendido no: aunque haya pasado su hora (un aplazado), se le pone fecha nueva y se reactiva
                alertas.deleteAll(lista);
            }
        });
    }

    private void comprobar(List<AlertaCuota> pendientes, Cuotas cuotas, LocalDateTime ahora) {
        for (AlertaCuota alerta : pendientes) {
            if (alerta.getUsuario().isEliminado()) {
                continue; // por si acaso: al eliminar la cuenta ya se borran sus alertas
            }
            BigDecimal actual = cuotas.de(alerta.getResultado());
            if (actual != null && actual.compareTo(alerta.getCuota()) >= 0) {
                alerta.avisar(ahora);
                Evento e = alerta.getEvento();
                notificaciones.avisar(alerta.getUsuario(), "alerta", "La cuota de «" + alerta.getResultado().getDescripcion()
                        + "» en " + e.getLocal().getNombre() + " – " + e.getVisitante().getNombre() + " ha llegado a "
                        + texto(actual) + " (pediste " + texto(alerta.getCuota()) + ").");
            }
        }
    }

    private static String texto(BigDecimal cuota) {
        return new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-ES"))).format(cuota);
    }
}

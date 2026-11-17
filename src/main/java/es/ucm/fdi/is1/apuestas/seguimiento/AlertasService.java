package es.ucm.fdi.is1.apuestas.seguimiento;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.eventos.EventoBorrado;
import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Cuotas;
import es.ucm.fdi.is1.apuestas.cuotas.CuotasCambiadas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
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
    private final CalculadoraCuotas calculadora;
    private final NotificacionService notificaciones;
    private final Clock reloj;

    public AlertasService(AlertaCuotaRepository alertas, EventoRepository eventos, UsuarioRepository usuarios,
                          CalculadoraCuotas calculadora, NotificacionService notificaciones, Clock reloj) {
        this.alertas = alertas;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.calculadora = calculadora;
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
        Evento evento = eventos.findById(eventoId).orElseThrow();
        if (!evento.admiteApuestas(ahora)) {
            throw new IllegalStateException("Este partido ya no admite apuestas");
        }
        BigDecimal actual = resultado == null ? null : calculadora.calcular(evento).de(resultado);
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
            comprobar(pendientes, cambio.cuotas(), LocalDateTime.now(reloj));
        }
    }

    /** Repaso de todas las alertas pendientes; las de partidos que ya no admiten apuestas se borran (CA-90.3). */
    @Scheduled(initialDelayString = "${apuestas.alertas.intervalo:PT10M}", fixedDelayString = "${apuestas.alertas.intervalo:PT10M}")
    @Transactional
    public void repasar() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        java.util.Map<Evento, List<AlertaCuota>> porEvento = new java.util.HashMap<>();
        alertas.findByAvisadaIsNull().forEach(a -> porEvento.computeIfAbsent(a.getEvento(), e -> new java.util.ArrayList<>()).add(a));
        porEvento.forEach((evento, lista) -> {
            if (!evento.admiteApuestas(ahora)) {
                alertas.deleteAll(lista);
            } else {
                comprobar(lista, calculadora.calcular(evento), ahora);
            }
        });
    }

    private void comprobar(List<AlertaCuota> pendientes, Cuotas cuotas, LocalDateTime ahora) {
        for (AlertaCuota alerta : pendientes) {
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

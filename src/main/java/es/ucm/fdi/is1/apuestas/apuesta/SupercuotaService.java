package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.eventos.EventoBorrado;
import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Cuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/**
 * Supercuotas (HU-87): el creador mejora durante unas horas la cuota de un resultado, con un importe máximo por
 * apuesta. Una por evento: una nueva sustituye a la anterior.
 */
@Service
public class SupercuotaService {

    /** Como mucho, el doble de la cuota normal: la casa no debe regalar el partido. */
    static final BigDecimal MEJORA_MAXIMA = new BigDecimal("2");
    static final int HORAS_MAXIMAS = 72;

    private final SupercuotaRepository supercuotas;
    private final EventoRepository eventos;
    private final VolumenApuestas volumen;
    private final CalculadoraCuotas calculadora;
    private final LimitesService limites;
    private final Clock reloj;

    public SupercuotaService(SupercuotaRepository supercuotas, EventoRepository eventos, VolumenApuestas volumen,
                             CalculadoraCuotas calculadora, LimitesService limites, Clock reloj) {
        this.supercuotas = supercuotas;
        this.eventos = eventos;
        this.volumen = volumen;
        this.calculadora = calculadora;
        this.limites = limites;
        this.reloj = reloj;
    }

    /**
     * Crea la supercuota (CA-87.1). Dura las horas indicadas o hasta que empiece el partido, lo que llegue antes.
     *
     * @throws IllegalArgumentException si la cuota no mejora la normal o pasa del doble, el importe máximo no está
     *                                  dentro de los límites de apuesta o las horas no van de 1 a 72
     * @throws IllegalStateException    si el evento ya no admite apuestas
     */
    @Transactional
    public Supercuota crear(Long eventoId, Resultado resultado, BigDecimal cuota, BigDecimal importeMaximo, Integer horas) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Evento evento = (eventoId == null ? java.util.Optional.<Evento>empty() : eventos.findById(eventoId))
                .orElseThrow(() -> new IllegalArgumentException("No existe ese evento"));
        if (!evento.admiteApuestas(ahora)) {
            throw new IllegalStateException("Este evento ya no admite apuestas");
        }
        BigDecimal normal = resultado == null ? null : calculadora.calcular(evento).de(resultado);
        if (normal == null) {
            throw new IllegalArgumentException("Elige un resultado que se pueda apostar en este evento");
        }
        BigDecimal tope = normal.multiply(MEJORA_MAXIMA).setScale(2, RoundingMode.DOWN);
        if (cuota == null || cuota.compareTo(normal) <= 0 || cuota.compareTo(tope) > 0 || cuota.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("La supercuota tiene que mejorar la normal (" + texto(normal)
                    + ") sin pasar del doble (" + texto(tope) + ")");
        }
        Limites actuales = limites.actuales();
        if (importeMaximo == null || importeMaximo.compareTo(actuales.getImporteMinimo()) < 0
                || importeMaximo.compareTo(actuales.getImporteMaximo()) > 0) {
            throw new IllegalArgumentException("El importe máximo tiene que estar entre " + texto(actuales.getImporteMinimo())
                    + " y " + texto(actuales.getImporteMaximo()) + " monedas");
        }
        if (horas == null || horas < 1 || horas > HORAS_MAXIMAS) {
            throw new IllegalArgumentException("La supercuota dura de 1 a " + HORAS_MAXIMAS + " horas");
        }
        supercuotas.findByEventoAndHastaAfter(evento, ahora).forEach(s -> s.terminar(ahora));
        LocalDateTime hasta = ahora.plusHours(horas);
        if (hasta.isAfter(evento.getFechaHora())) {
            hasta = evento.getFechaHora();
        }
        return supercuotas.save(new Supercuota(evento, resultado, cuota, importeMaximo, hasta, ahora));
    }

    /** Al borrar un evento creado por error, sus supercuotas se van con él. */
    @EventListener
    public void alBorrarse(EventoBorrado borrado) {
        supercuotas.deleteByEvento(borrado.evento());
    }

    /** La termina ya. */
    @Transactional
    public void quitar(Long eventoId) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Evento evento = eventos.findById(eventoId)
                .orElseThrow(() -> new es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException(eventoId));
        supercuotas.findByEventoAndHastaAfter(evento, ahora).forEach(s -> s.terminar(ahora));
    }

    /** La supercuota vigente del evento, si hay. */
    @Transactional(readOnly = true)
    public Optional<Supercuota> vigente(Evento evento) {
        return vigente(evento, LocalDateTime.now(reloj));
    }

    Optional<Supercuota> vigente(Evento evento, LocalDateTime ahora) {
        return supercuotas.findByEventoAndHastaAfter(evento, ahora).stream()
                .filter(s -> s.isVigente(ahora) && mejora(s)).findFirst();
    }

    /**
     * Sigue mejorando la cuota normal. Al crearla se exige que la mejore, pero la normal se mueve con el dinero
     * apostado y la forma de los equipos: si la alcanza, deja de valer y se paga la normal, que es mejor.
     */
    private boolean mejora(Supercuota supercuota) {
        return supercuota.getCuota().compareTo(
                calculadora.cuota(supercuota.getEvento(), supercuota.getResultado(), null)) > 0;
    }

    /** Las vigentes de todos los eventos, la que acaba antes primero: para la portada (CA-87.3). */
    @Transactional(readOnly = true)
    public List<Supercuota> vigentes() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<Supercuota> candidatas = supercuotas.findByHastaAfterOrderByHastaAsc(ahora).stream()
                .filter(s -> s.isVigente(ahora)).toList();
        Map<Long, Cuotas> normales = cuotasNormales(eventosUnicos(candidatas.stream()
                .map(Supercuota::getEvento).toList()));
        return candidatas.stream().filter(s -> mejora(s, normales.get(s.getEvento().getId()))).toList();
    }

    /** Las cuotas del evento tal como se enseñan: con la supercuota en su resultado, si la hay. */
    @Transactional(readOnly = true)
    public Cuotas cuotasConSupercuota(Evento evento) {
        if (evento.getId() == null) {
            return calculadora.calcular(evento);
        }
        return cuotasConSupercuota(List.of(evento)).get(evento.getId());
    }

    /** Cuotas de una lista de eventos con un solo acceso a volúmenes y otro a supercuotas. */
    @Transactional(readOnly = true)
    public Map<Long, Cuotas> cuotasConSupercuota(List<Evento> eventosVisibles) {
        return cuotasConSupercuota(eventosVisibles, true);
    }

    /** Opción para el boleto combinado: las supercuotas solo se aplican a apuestas simples. */
    @Transactional(readOnly = true)
    public Map<Long, Cuotas> cuotasConSupercuota(List<Evento> eventosVisibles, boolean aplicarSupercuotas) {
        List<Evento> unicos = eventosVisibles.stream().filter(e -> e.getId() != null)
                .collect(java.util.stream.Collectors.toMap(Evento::getId, e -> e, (a, b) -> a,
                        java.util.LinkedHashMap::new)).values().stream().toList();
        if (unicos.isEmpty()) {
            return Map.of();
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Map<Long, Cuotas> normales = cuotasNormales(unicos);
        if (!aplicarSupercuotas) {
            return normales;
        }
        Map<Long, Supercuota> porEvento = new HashMap<>();
        for (Supercuota s : supercuotas.findByEventoInAndHastaAfter(unicos, ahora)) {
            if (s.isVigente(ahora)) {
                porEvento.putIfAbsent(s.getEvento().getId(), s);
            }
        }
        Map<Long, Cuotas> resultado = new HashMap<>(normales);
        porEvento.forEach((eventoId, supercuota) -> {
            Cuotas cuotaNormal = normales.get(eventoId);
            if (mejora(supercuota, cuotaNormal)) {
                resultado.put(eventoId, sustituir(cuotaNormal, supercuota));
            }
        });
        return resultado;
    }

    private Map<Long, Cuotas> cuotasNormales(List<Evento> eventosVisibles) {
        Map<Long, Map<Resultado, BigDecimal>> volumenes = volumen.importes(eventosVisibles);
        Map<Long, Cuotas> cuotas = new HashMap<>();
        for (Evento evento : eventosVisibles) {
            cuotas.put(evento.getId(), calculadora.calcular(evento,
                    volumenes.getOrDefault(evento.getId(), Map.of())));
        }
        return cuotas;
    }

    private static Cuotas sustituir(Cuotas normales, Supercuota s) {
        return new Cuotas(s.getResultado() == Resultado.LOCAL ? s.getCuota() : normales.local(),
                s.getResultado() == Resultado.EMPATE ? s.getCuota() : normales.empate(),
                s.getResultado() == Resultado.VISITANTE ? s.getCuota() : normales.visitante());
    }

    private boolean mejora(Supercuota supercuota, Cuotas normales) {
        return normales != null && supercuota.getCuota().compareTo(normales.de(supercuota.getResultado())) > 0;
    }

    private static List<Evento> eventosUnicos(List<Evento> eventos) {
        return eventos.stream().collect(java.util.stream.Collectors.toMap(Evento::getId, e -> e, (a, b) -> a,
                java.util.LinkedHashMap::new)).values().stream().toList();
    }

    private static String texto(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.DOWN).toPlainString().replace('.', ',');
    }
}

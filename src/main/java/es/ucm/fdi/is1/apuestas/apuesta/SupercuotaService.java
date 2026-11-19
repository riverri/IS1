package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
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
    private final CalculadoraCuotas calculadora;
    private final LimitesService limites;
    private final Clock reloj;

    public SupercuotaService(SupercuotaRepository supercuotas, EventoRepository eventos, CalculadoraCuotas calculadora,
                             LimitesService limites, Clock reloj) {
        this.supercuotas = supercuotas;
        this.eventos = eventos;
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
        supercuotas.findByEventoAndHastaAfter(eventos.findById(eventoId).orElseThrow(), ahora).forEach(s -> s.terminar(ahora));
    }

    /** La supercuota vigente del evento, si hay. */
    @Transactional(readOnly = true)
    public Optional<Supercuota> vigente(Evento evento) {
        return vigente(evento, LocalDateTime.now(reloj));
    }

    Optional<Supercuota> vigente(Evento evento, LocalDateTime ahora) {
        return supercuotas.findByEventoAndHastaAfter(evento, ahora).stream().filter(s -> s.isVigente(ahora)).findFirst();
    }

    /** Las vigentes de todos los eventos, la que acaba antes primero: para la portada (CA-87.3). */
    @Transactional(readOnly = true)
    public List<Supercuota> vigentes() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<Supercuota> lista = supercuotas.findByHastaAfterOrderByHastaAsc(ahora).stream().filter(s -> s.isVigente(ahora)).toList();
        lista.forEach(s -> s.getEvento().getLocal().getNombre());
        return lista;
    }

    /** Las cuotas del evento tal como se enseñan: con la supercuota en su resultado, si la hay. */
    @Transactional(readOnly = true)
    public Cuotas cuotasConSupercuota(Evento evento) {
        Cuotas normales = calculadora.calcular(evento);
        return vigente(evento).map(s -> new Cuotas(
                s.getResultado() == Resultado.LOCAL ? s.getCuota() : normales.local(),
                s.getResultado() == Resultado.EMPATE ? s.getCuota() : normales.empate(),
                s.getResultado() == Resultado.VISITANTE ? s.getCuota() : normales.visitante())).orElse(normales);
    }

    private static String texto(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.DOWN).toPlainString().replace('.', ',');
    }
}

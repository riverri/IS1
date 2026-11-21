package es.ucm.fdi.is1.apuestas.cuotas;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.context.event.EventListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.eventos.EventoBorrado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.web.GraficoLinea;

/**
 * Apunta las cuotas de un evento cada vez que cambian (HU-91). Las cuotas se mueven con el dinero apostado, así que
 * se llama después de apostar, cancelar o cambiar un importe, y al abrir la página del partido (por si han cambiado
 * los equipos o su forma). Cuando cambian, publica {@link CuotasCambiadas}.
 */
@Component
public class RegistroCuotas {

    private final CalculadoraCuotas calculadora;
    private final CuotaHistoricaRepository historico;
    private final ApplicationEventPublisher eventos;
    private final Clock reloj;

    public RegistroCuotas(CalculadoraCuotas calculadora, CuotaHistoricaRepository historico,
                          ApplicationEventPublisher eventos, Clock reloj) {
        this.calculadora = calculadora;
        this.historico = historico;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    /** Guarda las cuotas actuales si son distintas de las últimas apuntadas. */
    @Transactional
    public void registrar(Evento evento) {
        Cuotas actuales = calculadora.calcular(evento);
        boolean cambian = historico.findTopByEventoOrderByFechaDescIdDesc(evento)
                .map(ultima -> !ultima.iguales(actuales)).orElse(true);
        if (cambian) {
            historico.save(new CuotaHistorica(evento, actuales, LocalDateTime.now(reloj)));
            eventos.publishEvent(new CuotasCambiadas(evento.getId(), actuales));
        }
    }

    /** Al borrar un evento creado por error, su historial se va con él. */
    @EventListener
    public void alBorrarse(EventoBorrado borrado) {
        historico.deleteByEvento(borrado.evento());
    }

    /**
     * Las líneas del gráfico de la evolución (CA-91.1): una por resultado, con la misma escala para poder
     * compararlas. Null si aún no ha cambiado ninguna vez (hace falta más de un punto).
     */
    @Transactional(readOnly = true)
    public Evolucion evolucion(Evento evento) {
        List<CuotaHistorica> puntos = historico.findByEventoOrderByFechaAscIdAsc(evento);
        if (puntos.size() < 2) {
            return null;
        }
        List<Double> local = puntos.stream().map(p -> p.getCuotas().local().doubleValue()).toList();
        List<Double> visitante = puntos.stream().map(p -> p.getCuotas().visitante().doubleValue()).toList();
        List<Double> empate = puntos.get(0).getCuotas().empate() == null ? null
                : puntos.stream().map(p -> p.getCuotas().empate().doubleValue()).toList();
        double minimo = Stream.of(local, visitante, empate == null ? List.<Double>of() : empate)
                .flatMap(List::stream).mapToDouble(Double::doubleValue).min().orElse(1);
        double maximo = Stream.of(local, visitante, empate == null ? List.<Double>of() : empate)
                .flatMap(List::stream).mapToDouble(Double::doubleValue).max().orElse(2);
        return new Evolucion(GraficoLinea.de(local, minimo, maximo),
                empate == null ? null : GraficoLinea.de(empate, minimo, maximo),
                GraficoLinea.de(visitante, minimo, maximo), puntos.size(), puntos.get(0).getCuotas(),
                puntos.get(puntos.size() - 1).getCuotas(), minimo, maximo);
    }

    /** Gráfico de la evolución de las cuotas 1X2 de un evento: primeras, actuales y una línea por resultado. */
    public record Evolucion(GraficoLinea local, GraficoLinea empate, GraficoLinea visitante, int cambios,
                            Cuotas primeras, Cuotas ultimas, double minimo, double maximo) {
    }
}

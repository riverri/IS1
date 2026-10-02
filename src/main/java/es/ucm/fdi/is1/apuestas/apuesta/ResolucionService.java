package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Cuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/** Acciones del creador de apuestas sobre un evento y sus apuestas (HU-04, HU-05, HU-06, HU-25). */
@Service
public class ResolucionService {

    private final EventoRepository eventos;
    private final SeleccionRepository selecciones;
    private final AvisosApuestas avisos;
    private final CalculadoraCuotas calculadora;

    public ResolucionService(EventoRepository eventos, SeleccionRepository selecciones, AvisosApuestas avisos,
                             CalculadoraCuotas calculadora) {
        this.eventos = eventos;
        this.selecciones = selecciones;
        this.avisos = avisos;
        this.calculadora = calculadora;
    }

    @Transactional(readOnly = true)
    public Evento evento(Long id) {
        return eventos.findById(id).orElseThrow(() -> new EventoNoDisponibleException(id));
    }

    /**
     * Introduce o corrige el resultado: el evento pasa a finalizado y todas sus apuestas
     * se resuelven y se pagan. Si se corrige, los saldos se reajustan (HU-04, HU-25).
     */
    @Transactional
    public int introducirResultado(Long eventoId, Resultado resultado) {
        Evento evento = evento(eventoId);
        evento.finalizar(resultado);
        return resolverApuestas(evento);
    }

    /**
     * Igual, pero con el marcador final de un partido de fútbol: además del 1X2 se resuelven las apuestas
     * de goles y de ambos marcan (HU-52). Sin marcador, esas se anulan.
     */
    @Transactional
    public int introducirMarcador(Long eventoId, int golesLocal, int golesVisitante) {
        Evento evento = evento(eventoId);
        evento.finalizarConMarcador(golesLocal, golesVisitante);
        return resolverApuestas(evento);
    }

    private int resolverApuestas(Evento evento) {
        List<Seleccion> afectadas = selecciones.findByEvento(evento);
        for (Seleccion seleccion : afectadas) {
            Apuesta apuesta = seleccion.getApuesta();
            EstadoApuesta antes = apuesta.getEstado();
            apuesta.resolver(evento, evento.getResultado());
            avisos.siCambia(apuesta, antes);
        }
        return afectadas.size();
    }

    @Transactional
    public void suspender(Long eventoId) {
        evento(eventoId).suspender();
    }

    @Transactional
    public void reactivar(Long eventoId) {
        evento(eventoId).reactivar();
    }

    /** Anula el evento y devuelve el importe de todas sus apuestas activas (HU-05). */
    @Transactional
    public int anular(Long eventoId) {
        Evento evento = evento(eventoId);
        evento.anular();
        List<Seleccion> afectadas = selecciones.findByEvento(evento);
        for (Seleccion seleccion : afectadas) {
            Apuesta apuesta = seleccion.getApuesta();
            EstadoApuesta antes = apuesta.getEstado();
            apuesta.anular(evento);
            avisos.siCambia(apuesta, antes);
        }
        return afectadas.size();
    }

    /**
     * Importe total y número de apuestas de cada opción del evento (HU-06): primero el 1X2 y después,
     * en fútbol, la doble oportunidad, los goles y ambos marcan (HU-52).
     * En una combinada cuenta su importe completo, porque depende de esta selección.
     */
    @Transactional(readOnly = true)
    public List<VolumenResultado> volumen(Long eventoId) {
        Evento evento = evento(eventoId);
        List<Seleccion> delEvento = selecciones.findByEvento(evento).stream()
                .filter(s -> s.getApuesta().getEstado() != EstadoApuesta.CANCELADA
                        && s.getApuesta().getEstado() != EstadoApuesta.ANULADA)
                .toList();
        Cuotas cuotas = calculadora.calcular(evento);
        List<VolumenResultado> volumen = new ArrayList<>();
        for (Resultado resultado : Resultado.values()) {
            if (resultado == Resultado.EMPATE && !evento.getDeporte().isAdmiteEmpate()) {
                continue;
            }
            volumen.add(fila(resultado.getSimbolo(), resultado.getDescripcion(), cuotas.de(resultado),
                    delEvento.stream().filter(s -> s.getPronostico() == resultado).toList()));
        }
        Map<Especial, BigDecimal> especiales = calculadora.especiales(evento);
        for (Especial especial : Especial.values()) {
            List<Seleccion> aEste = delEvento.stream().filter(s -> s.getEspecial() == especial).toList();
            if (especiales.containsKey(especial) || !aEste.isEmpty()) {
                volumen.add(fila(especial.getSimbolo(), especial.getDescripcion(), especiales.get(especial), aEste));
            }
        }
        return volumen;
    }

    private static VolumenResultado fila(String simbolo, String descripcion, BigDecimal cuota,
                                         List<Seleccion> aEsta) {
        BigDecimal total = aEsta.stream().map(s -> s.getApuesta().getImporte())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new VolumenResultado(simbolo, descripcion, cuota, total, aEsta.size());
    }
}

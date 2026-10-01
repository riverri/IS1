package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/** Acciones del creador de apuestas sobre un evento y sus apuestas (HU-04, HU-05, HU-06, HU-25). */
@Service
public class ResolucionService {

    private final EventoRepository eventos;
    private final SeleccionRepository selecciones;

    public ResolucionService(EventoRepository eventos, SeleccionRepository selecciones) {
        this.eventos = eventos;
        this.selecciones = selecciones;
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
        List<Seleccion> afectadas = selecciones.findByEvento(evento);
        afectadas.forEach(s -> s.getApuesta().resolver(evento, resultado));
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
        afectadas.forEach(s -> s.getApuesta().anular(evento));
        return afectadas.size();
    }

    /**
     * Importe total y número de apuestas de cada resultado posible del evento (HU-06).
     * En una combinada cuenta su importe completo, porque depende de este resultado.
     */
    @Transactional(readOnly = true)
    public List<VolumenResultado> volumen(Long eventoId) {
        Evento evento = evento(eventoId);
        List<Seleccion> delEvento = selecciones.findByEvento(evento).stream()
                .filter(s -> s.getApuesta().getEstado() != EstadoApuesta.CANCELADA
                        && s.getApuesta().getEstado() != EstadoApuesta.ANULADA)
                .toList();
        List<VolumenResultado> volumen = new ArrayList<>();
        for (Resultado resultado : Resultado.values()) {
            if (resultado == Resultado.EMPATE && !evento.getDeporte().isAdmiteEmpate()) {
                continue;
            }
            List<Seleccion> aEste = delEvento.stream().filter(s -> s.getPronostico() == resultado).toList();
            BigDecimal total = aEste.stream().map(s -> s.getApuesta().getImporte())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            volumen.add(new VolumenResultado(resultado, total, aEste.size()));
        }
        return volumen;
    }
}

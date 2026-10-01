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
    private final ApuestaRepository apuestas;

    public ResolucionService(EventoRepository eventos, ApuestaRepository apuestas) {
        this.eventos = eventos;
        this.apuestas = apuestas;
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
        List<Apuesta> afectadas = apuestas.findByEvento(evento);
        afectadas.forEach(a -> a.resolver(resultado));
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
        List<Apuesta> afectadas = apuestas.findByEvento(evento);
        afectadas.forEach(Apuesta::anular);
        return afectadas.size();
    }

    /** Importe total y número de apuestas de cada resultado posible del evento (HU-06). */
    @Transactional(readOnly = true)
    public List<VolumenResultado> volumen(Long eventoId) {
        Evento evento = evento(eventoId);
        List<Apuesta> delEvento = apuestas.findByEvento(evento).stream()
                .filter(a -> a.getEstado() != EstadoApuesta.CANCELADA && a.getEstado() != EstadoApuesta.ANULADA)
                .toList();
        List<VolumenResultado> volumen = new ArrayList<>();
        for (Resultado resultado : Resultado.values()) {
            if (resultado == Resultado.EMPATE && !evento.getDeporte().isAdmiteEmpate()) {
                continue;
            }
            List<Apuesta> aEste = delEvento.stream().filter(a -> a.getPronostico() == resultado).toList();
            BigDecimal total = aEste.stream().map(Apuesta::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
            volumen.add(new VolumenResultado(resultado, total, aEste.size()));
        }
        return volumen;
    }
}

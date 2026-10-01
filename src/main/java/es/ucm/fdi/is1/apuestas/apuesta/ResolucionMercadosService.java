package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.mercados.Candidato;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;
import es.ucm.fdi.is1.apuestas.mercados.MercadoService;

/** Ganador y anulación de los mercados a largo plazo, y pago de sus apuestas (HU-45). */
@Service
public class ResolucionMercadosService {

    private final MercadoService mercados;
    private final SeleccionRepository selecciones;

    public ResolucionMercadosService(MercadoService mercados, SeleccionRepository selecciones) {
        this.mercados = mercados;
        this.selecciones = selecciones;
    }

    /** Marca (o corrige) el ganador y paga las apuestas acertadas. Devuelve cuántas se han resuelto. */
    @Transactional
    public int resolver(Long mercadoId, Long candidatoId) {
        Mercado mercado = mercados.mercado(mercadoId);
        Candidato ganador = mercado.getCandidatos().stream()
                .filter(c -> c.getId().equals(candidatoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ese candidato no es de este mercado"));
        mercado.resolver(ganador);
        List<Seleccion> afectadas = selecciones.findByCandidatoMercado(mercado);
        afectadas.forEach(s -> s.getApuesta().resolver(mercado, ganador));
        return afectadas.size();
    }

    /** Anula el mercado y devuelve el importe de todas sus apuestas. */
    @Transactional
    public int anular(Long mercadoId) {
        Mercado mercado = mercados.mercado(mercadoId);
        mercado.anular();
        List<Seleccion> afectadas = selecciones.findByCandidatoMercado(mercado);
        afectadas.forEach(s -> s.getApuesta().anular(mercado));
        return afectadas.size();
    }

    /** Número de apuestas e importe apostado a cada candidato, sin contar las canceladas. */
    @Transactional(readOnly = true)
    public List<VolumenCandidato> volumen(Long mercadoId) {
        Mercado mercado = mercados.mercado(mercadoId);
        List<Seleccion> delMercado = selecciones.findByCandidatoMercado(mercado).stream()
                .filter(s -> s.getApuesta().getEstado() != EstadoApuesta.CANCELADA)
                .toList();
        return mercado.getCandidatos().stream().map(candidato -> {
            List<Seleccion> aEste = delMercado.stream()
                    .filter(s -> s.getCandidato().getId().equals(candidato.getId()))
                    .toList();
            BigDecimal total = aEste.stream().map(s -> s.getApuesta().getImporte())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new VolumenCandidato(candidato, total, aEste.size());
        }).toList();
    }

    public record VolumenCandidato(Candidato candidato, BigDecimal importe, int apuestas) {
    }
}

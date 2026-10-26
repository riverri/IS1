package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.mercados.Candidato;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;
import es.ucm.fdi.is1.apuestas.mercados.MercadoService;

/**
 * Ganador y anulación de los mercados a largo plazo, y pago de sus apuestas (HU-45). Lo usa el panel de
 * gestión del creador. El {@link Mercado} cambia de estado y cada {@link Apuesta} afectada se recalcula y
 * ajusta el saldo de su usuario; todo en la misma transacción.
 */
@Service
public class ResolucionMercadosService {

    private final MercadoService mercados;
    private final SeleccionRepository selecciones;
    private final AvisosApuestas avisos;

    public ResolucionMercadosService(MercadoService mercados, SeleccionRepository selecciones,
                                     AvisosApuestas avisos) {
        this.mercados = mercados;
        this.selecciones = selecciones;
        this.avisos = avisos;
    }

    /**
     * Marca (o corrige) el ganador y paga las apuestas acertadas; el resto pasan a perdidas (CA-45.3).
     * Si se corrige, la apuesta solo abona o retira la diferencia con lo ya pagado. Devuelve cuántas se han resuelto.
     *
     * @throws IllegalArgumentException si el candidato no es de este mercado
     * @throws IllegalStateException    si el mercado está anulado
     */
    @Transactional
    public int resolver(Long mercadoId, Long candidatoId) {
        Mercado mercado = mercados.mercado(mercadoId);
        Candidato ganador = mercado.getCandidatos().stream()
                .filter(c -> c.getId().equals(candidatoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ese candidato no es de este mercado"));
        mercado.resolver(ganador);
        List<Seleccion> afectadas = selecciones.findByCandidatoMercado(mercado);
        for (Seleccion seleccion : afectadas) {
            Apuesta apuesta = seleccion.getApuesta();
            // Se guarda el estado anterior para avisar solo si cambia (HU-37)
            EstadoApuesta antes = apuesta.getEstado();
            apuesta.resolver(mercado, ganador);
            avisos.siCambia(apuesta, antes);
        }
        return afectadas.size();
    }

    /**
     * Anula el mercado y devuelve el importe de todas sus apuestas (las canceladas ya lo recuperaron).
     *
     * @throws IllegalStateException si el mercado ya estaba resuelto o anulado
     */
    @Transactional
    public int anular(Long mercadoId) {
        Mercado mercado = mercados.mercado(mercadoId);
        mercado.anular();
        List<Seleccion> afectadas = selecciones.findByCandidatoMercado(mercado);
        for (Seleccion seleccion : afectadas) {
            Apuesta apuesta = seleccion.getApuesta();
            EstadoApuesta antes = apuesta.getEstado();
            apuesta.anular(mercado);
            avisos.siCambia(apuesta, antes);
        }
        return afectadas.size();
    }

    /** Número de apuestas e importe apostado a cada candidato, sin contar las canceladas ni las anuladas. */
    @Transactional(readOnly = true)
    public List<VolumenCandidato> volumen(Long mercadoId) {
        Mercado mercado = mercados.mercado(mercadoId);
        List<Seleccion> delMercado = selecciones.findByCandidatoMercado(mercado).stream()
                // Mismo criterio que en los eventos: ni canceladas ni anuladas (se devolvió el importe)
                .filter(s -> s.getApuesta().getEstado() != EstadoApuesta.CANCELADA
                        && s.getApuesta().getEstado() != EstadoApuesta.ANULADA)
                .toList();
        // Una lambda con llaves { } puede tener varias líneas; devuelve lo que pone su "return"
        return mercado.getCandidatos().stream().map(candidato -> {
            List<Seleccion> aEste = delMercado.stream()
                    .filter(s -> s.getCandidato().getId().equals(candidato.getId()))
                    .toList();
            BigDecimal total = aEste.stream().map(s -> s.getApuesta().getImporte())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new VolumenCandidato(candidato, total, aEste.size());
        }).toList();
    }

    /** Una fila del volumen de un mercado: el candidato, lo apostado a él y en cuántas apuestas. */
    public record VolumenCandidato(Candidato candidato, BigDecimal importe, int apuestas) {
    }
}

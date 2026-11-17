package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/**
 * Cuánto pagaría la casa con cada resultado de un evento (HU-76). Es un peor caso:
 * <ul>
 * <li>una combinada se paga entera si acierta la selección de este evento, salvo que ya haya fallado otra;</li>
 * <li>las de goles y ambos marcan dependen del marcador, así que se suponen ganadas con cualquier resultado;</li>
 * <li>las apuestas canceladas y las anuladas no cuentan: se devolvió el importe.</li>
 * </ul>
 */
@Service
public class RiesgoService {

    private final SeleccionRepository selecciones;
    private final EventoRepository eventos;
    private final LimitesService limites;

    public RiesgoService(SeleccionRepository selecciones, EventoRepository eventos, LimitesService limites) {
        this.selecciones = selecciones;
        this.eventos = eventos;
        this.limites = limites;
    }

    /** Riesgo de un evento para su ficha en Gestión (CA-76.1 y CA-76.2). */
    @Transactional(readOnly = true)
    public RiesgoEvento delEvento(Long eventoId) {
        Evento evento = eventos.findById(eventoId).orElseThrow();
        List<Seleccion> delEvento = selecciones.findByEvento(evento).stream()
                .filter(s -> s.getApuesta().getEstado() != EstadoApuesta.CANCELADA
                        && s.getApuesta().getEstado() != EstadoApuesta.ANULADA)
                .toList();
        return calcular(evento, delEvento, limites.actuales().getAlertaRiesgo());
    }

    /**
     * Ids de los eventos en alerta, para marcarlos en el panel (CA-76.3). Solo cuentan las apuestas activas: un
     * evento ya resuelto no tiene riesgo. Una sola consulta para todos los eventos.
     */
    @Transactional(readOnly = true)
    public Set<Long> eventosEnAlerta() {
        BigDecimal limite = limites.actuales().getAlertaRiesgo();
        Map<Evento, List<Seleccion>> porEvento = selecciones.activasDeEventos().stream()
                .collect(Collectors.groupingBy(Seleccion::getEvento));
        return porEvento.entrySet().stream()
                .filter(e -> calcular(e.getKey(), e.getValue(), limite).isAlerta())
                .map(e -> e.getKey().getId())
                .collect(Collectors.toSet());
    }

    private static RiesgoEvento calcular(Evento evento, List<Seleccion> delEvento, BigDecimal limite) {
        // Cada apuesta una vez, aunque tenga dos selecciones del mismo evento
        Set<Apuesta> apuestas = new LinkedHashSet<>();
        delEvento.forEach(s -> apuestas.add(s.getApuesta()));
        BigDecimal apostado = apuestas.stream().map(Apuesta::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RiesgoResultado> filas = new ArrayList<>();
        for (Resultado resultado : Resultado.values()) {
            if (resultado == Resultado.EMPATE && !evento.getDeporte().isAdmiteEmpate()) {
                continue;
            }
            Set<Apuesta> cobran = new LinkedHashSet<>();
            delEvento.stream()
                    .filter(s -> gana(s, resultado) && !falloOtra(s))
                    .forEach(s -> cobran.add(s.getApuesta()));
            BigDecimal pago = cobran.stream().map(Apuesta::getGananciaPotencial).reduce(BigDecimal.ZERO, BigDecimal::add);
            filas.add(new RiesgoResultado(resultado.getSimbolo(), resultado.getDescripcion(), pago,
                    apostado.subtract(pago)));
        }
        return new RiesgoEvento(apostado, filas, limite);
    }

    /** Si la selección se cobra con ese resultado; las que necesitan el marcador, siempre (peor caso). */
    private static boolean gana(Seleccion seleccion, Resultado resultado) {
        if (seleccion.getEspecial() != null) {
            return seleccion.getEspecial().isNecesitaMarcador()
                    || Boolean.TRUE.equals(seleccion.getEspecial().acierta(resultado, null, null));
        }
        return seleccion.getPronostico() == resultado;
    }

    /** Una combinada con otra selección ya fallada no se va a pagar. */
    private static boolean falloOtra(Seleccion seleccion) {
        return seleccion.getApuesta().getSelecciones().stream()
                .anyMatch(o -> o != seleccion && o.getEstado() == EstadoSeleccion.FALLADA);
    }
}

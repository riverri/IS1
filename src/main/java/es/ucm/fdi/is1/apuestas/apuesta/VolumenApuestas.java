package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.cuotas.VolumenApostado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** Dinero de las apuestas activas en cada resultado, para el ajuste de cuotas por volumen. */
@Component
class VolumenApuestas implements VolumenApostado {

    private final SeleccionRepository selecciones;

    VolumenApuestas(SeleccionRepository selecciones) {
        this.selecciones = selecciones;
    }

    @Override
    public Map<Resultado, BigDecimal> importes(Evento evento) {
        Map<Resultado, BigDecimal> importes = new EnumMap<>(Resultado.class);
        if (evento.getId() == null) {
            return importes;
        }
        for (Object[] fila : selecciones.importesActivosPorPronostico(evento)) {
            importes.put((Resultado) fila[0], (BigDecimal) fila[1]);
        }
        return importes;
    }
}

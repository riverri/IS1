package es.ucm.fdi.is1.apuestas.cuotas;

import java.math.BigDecimal;
import java.util.Map;

import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** Dinero en juego en cada resultado de un evento, para ajustar las cuotas (fila 3 del backlog). */
public interface VolumenApostado {

    /** Importe de las apuestas activas a cada resultado; los resultados sin apuestas pueden faltar. */
    Map<Resultado, BigDecimal> importes(Evento evento);
}

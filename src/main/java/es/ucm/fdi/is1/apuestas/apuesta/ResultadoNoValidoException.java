package es.ucm.fdi.is1.apuestas.apuesta;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

/**
 * El pronóstico pedido no se puede apostar en ese evento: un empate en un deporte sin empate, un tipo
 * especial fuera del fútbol (HU-52) o no se ha elegido nada. La lanza {@link ApuestaService} al calcular la cuota.
 */
public class ResultadoNoValidoException extends RuntimeException {

    /** Empate en un deporte que no lo admite. */
    public ResultadoNoValidoException(Resultado resultado) {
        super("El resultado " + resultado.getDescripcion() + " no es uno de los resultados posibles en este evento");
    }

    /** Doble oportunidad, goles o ambos marcan en un deporte que no es fútbol. */
    public ResultadoNoValidoException(Especial especial) {
        super("La apuesta \"" + especial.getDescripcion() + "\" solo está disponible en los partidos de fútbol");
    }

    /** Ni resultado ni tipo especial, o los dos a la vez. */
    public ResultadoNoValidoException() {
        super("Elige un pronóstico:");
    }

    /** La excepción adecuada cuando no hay cuota para esa combinación de resultado y tipo especial. */
    static ResultadoNoValidoException de(Resultado resultado, Especial especial) {
        if (resultado != null && especial != null || resultado == null && especial == null) {
            return new ResultadoNoValidoException();
        }
        return especial != null ? new ResultadoNoValidoException(especial) : new ResultadoNoValidoException(resultado);
    }
}

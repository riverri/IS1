package es.ucm.fdi.is1.apuestas.apuesta;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

public class ResultadoNoValidoException extends RuntimeException {

    public ResultadoNoValidoException(Resultado resultado) {
        super("El resultado " + resultado.getDescripcion() + " no es posible en este evento");
    }

    public ResultadoNoValidoException(Especial especial) {
        super("La apuesta \"" + especial.getDescripcion() + "\" solo está disponible en los partidos de fútbol");
    }

    /** Ni resultado ni tipo especial, o los dos a la vez. */
    public ResultadoNoValidoException() {
        super("Elige un pronóstico");
    }

    static ResultadoNoValidoException de(Resultado resultado, Especial especial) {
        if (resultado != null && especial != null || resultado == null && especial == null) {
            return new ResultadoNoValidoException();
        }
        return especial != null ? new ResultadoNoValidoException(especial) : new ResultadoNoValidoException(resultado);
    }
}

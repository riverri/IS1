package es.ucm.fdi.is1.apuestas.apuesta;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

public class ResultadoNoValidoException extends RuntimeException {

    public ResultadoNoValidoException(Resultado resultado) {
        super("El resultado " + resultado.getDescripcion() + " no es posible en este evento");
    }
}

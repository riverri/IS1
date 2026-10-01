package es.ucm.fdi.is1.apuestas.eventos;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;

/** Resultado de un partido visto desde uno de los dos equipos (HU-31). */
public enum ResultadoEquipo {

    VICTORIA("V", "Victoria"),
    EMPATE("E", "Empate"),
    DERROTA("D", "Derrota");

    private final String letra;
    private final String descripcion;

    ResultadoEquipo(String letra, String descripcion) {
        this.letra = letra;
        this.descripcion = descripcion;
    }

    /** Resultado de un evento finalizado para uno de sus equipos. */
    public static ResultadoEquipo de(Evento evento, Equipo equipo) {
        if (evento.getResultado() == Resultado.EMPATE) {
            return EMPATE;
        }
        boolean ganaLocal = evento.getResultado() == Resultado.LOCAL;
        boolean esLocal = evento.getLocal().getId().equals(equipo.getId());
        return ganaLocal == esLocal ? VICTORIA : DERROTA;
    }

    public String getLetra() {
        return letra;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

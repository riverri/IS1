package es.ucm.fdi.is1.apuestas.eventos;

import java.util.List;

import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;

/**
 * Ficha de un equipo o deportista (HU-31): últimos resultados, balance, racha, posición en cada competición
 * según los resultados registrados en la web y próximos partidos.
 */
public record FichaEquipo(Equipo equipo, List<PartidoJugado> ultimos, Balance balance, List<ResultadoEquipo> racha,
                          List<Puesto> clasificaciones, List<Evento> proximos) {

    /** Un partido ya jugado desde el punto de vista del equipo. */
    public record PartidoJugado(Evento evento, Equipo rival, boolean enCasa, ResultadoEquipo resultado) {
    }

    /** Posición en la clasificación de una competición, calculada con los partidos finalizados. */
    public record Puesto(Competicion competicion, int posicion, int participantes, Balance balance) {
    }
}

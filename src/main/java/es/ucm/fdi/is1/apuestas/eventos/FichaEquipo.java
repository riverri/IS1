package es.ucm.fdi.is1.apuestas.eventos;

import java.util.List;
import java.util.Map;

import es.ucm.fdi.is1.apuestas.equipos.Alineacion;
import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;

/**
 * Ficha de un equipo o deportista (HU-31): últimos resultados, balance, racha, posición en cada competición
 * según los resultados registrados en la web, próximos partidos, gráfico de evolución (HU-32),
 * plantilla y alineación probable (HU-49; null si no hay jugadores suficientes).
 *
 * <p>Objeto de solo lectura que monta {@link FichaEquipoService#ficha(Long)} y se pasa entero a la
 * plantilla {@code equipo.html}.
 *
 * @param ultimos         últimos partidos finalizados, del más reciente al más antiguo
 * @param balance         balance de todos los partidos finalizados del equipo
 * @param racha           últimos resultados, del más antiguo al más reciente
 * @param clasificaciones puesto en cada competición en la que ya ha jugado, por nombre de competición
 * @param proximos        próximos partidos en los que aún se puede apostar
 * @param evolucion       null si ha jugado menos de dos partidos
 * @param plantilla       jugadores agrupados por posición; vacía si no hay jugadores
 */
public record FichaEquipo(Equipo equipo, List<PartidoJugado> ultimos, Balance balance, List<ResultadoEquipo> racha,
                          List<Puesto> clasificaciones, List<Evento> proximos, GraficoEvolucion evolucion,
                          Map<Posicion, List<Jugador>> plantilla, Alineacion alineacion) {

    /** Un partido ya jugado desde el punto de vista del equipo. */
    public record PartidoJugado(Evento evento, Equipo rival, boolean enCasa, ResultadoEquipo resultado) {
    }

    /** Posición en la clasificación de una competición, calculada con los partidos finalizados. */
    public record Puesto(Competicion competicion, int posicion, int participantes, Balance balance) {
    }
}

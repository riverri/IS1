package es.ucm.fdi.is1.apuestas.fantasy;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/**
 * Cuántos puntos da a un jugador de la liga fantasy un partido ya jugado de su equipo (HU-82). No hay estadísticas
 * de cada jugador (goles, tarjetas…), así que todo sale del resultado de su equipo y de su posición:
 * <ul>
 *   <li>Victoria: 3 puntos; empate: 1.</li>
 *   <li>Portería a cero: portero +4, defensa +3, centrocampista +1.</li>
 *   <li>Cada gol de su equipo: delantero +2, centrocampista +1.</li>
 *   <li>Si su equipo encaja 3 o más: portero y defensa −1.</li>
 * </ul>
 * Sin marcador (el creador solo metió el 1X2) solo cuentan la victoria y el empate.
 */
public final class PuntosFantasy {

    public static final int VICTORIA = 3;
    public static final int EMPATE = 1;

    private PuntosFantasy() {
        // solo métodos estáticos
    }

    /** Puntos del partido para un jugador de esa posición que juega en ese equipo. */
    public static int puntos(Posicion posicion, Equipo equipo, Evento partido) {
        boolean local = partido.getLocal().getId().equals(equipo.getId());
        Resultado resultado = partido.getResultado();
        int puntos = 0;
        if (resultado == Resultado.EMPATE) {
            puntos += EMPATE;
        } else if (resultado == (local ? Resultado.LOCAL : Resultado.VISITANTE)) {
            puntos += VICTORIA;
        }
        if (!partido.isConMarcador()) {
            return puntos;
        }
        int aFavor = local ? partido.getGolesLocal() : partido.getGolesVisitante();
        int enContra = local ? partido.getGolesVisitante() : partido.getGolesLocal();
        if (enContra == 0) {
            puntos += switch (posicion) {
                case PORTERO -> 4;
                case DEFENSA -> 3;
                case CENTROCAMPISTA -> 1;
                case DELANTERO -> 0;
            };
        }
        puntos += aFavor * switch (posicion) {
            case DELANTERO -> 2;
            case CENTROCAMPISTA -> 1;
            default -> 0;
        };
        if (enContra >= 3 && (posicion == Posicion.PORTERO || posicion == Posicion.DEFENSA)) {
            puntos -= 1;
        }
        return puntos;
    }
}

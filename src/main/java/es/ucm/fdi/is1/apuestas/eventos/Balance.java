package es.ucm.fdi.is1.apuestas.eventos;

import java.util.Collection;

/** Victorias, empates y derrotas de un equipo en una serie de partidos (HU-31, HU-33). */
public record Balance(int victorias, int empates, int derrotas) {

    public static Balance de(Collection<ResultadoEquipo> resultados) {
        int v = 0;
        int e = 0;
        int d = 0;
        for (ResultadoEquipo resultado : resultados) {
            switch (resultado) {
                case VICTORIA -> v++;
                case EMPATE -> e++;
                case DERROTA -> d++;
            }
        }
        return new Balance(v, e, d);
    }

    public int jugados() {
        return victorias + empates + derrotas;
    }

    /** 3 puntos por victoria y 1 por empate. */
    public int puntos() {
        return 3 * victorias + empates;
    }
}

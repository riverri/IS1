package es.ucm.fdi.is1.apuestas.eventos;

import java.util.List;

/**
 * Comparación de los dos rivales de un evento (HU-33): enfrentamientos anteriores entre ellos
 * y la racha reciente de cada uno.
 */
public record CaraACara(Evento evento, List<Evento> anteriores, Balance balanceLocal,
                        List<ResultadoEquipo> rachaLocal, List<ResultadoEquipo> rachaVisitante) {

    public boolean hayDatos() {
        return !anteriores.isEmpty() || !rachaLocal.isEmpty() || !rachaVisitante.isEmpty();
    }
}

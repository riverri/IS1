package es.ucm.fdi.is1.apuestas.eventos;

import java.util.List;

/**
 * Comparación de los dos rivales de un evento (HU-33): enfrentamientos anteriores entre ellos
 * y la racha reciente de cada uno. La construye {@link FichaEquipoService#caraACara(Evento)} y se
 * muestra en la página de apostar de un evento.
 *
 * @param evento         el partido que se va a jugar
 * @param anteriores     últimos partidos finalizados entre los dos, del más reciente al más antiguo
 * @param balanceLocal   balance de todos esos enfrentamientos visto desde el equipo local del evento
 * @param rachaLocal     últimos resultados del local, del más antiguo al más reciente
 * @param rachaVisitante últimos resultados del visitante, del más antiguo al más reciente
 */
public record CaraACara(Evento evento, List<Evento> anteriores, Balance balanceLocal,
                        List<ResultadoEquipo> rachaLocal, List<ResultadoEquipo> rachaVisitante) {

    /** True si hay algo que comparar: enfrentamientos previos o resultados recientes de alguno de los dos. */
    public boolean hayDatos() {
        return !anteriores.isEmpty() || !rachaLocal.isEmpty() || !rachaVisitante.isEmpty();
    }
}

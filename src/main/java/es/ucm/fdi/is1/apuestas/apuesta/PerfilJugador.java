package es.ucm.fdi.is1.apuestas.apuesta;

/**
 * Lo que se ve de otro jugador (HU-48): su puesto, sus cifras y cuántas apuestas tiene en juego.
 * Lo construye {@link RankingService#perfil}; no incluye sus apuestas.
 *
 * @param puesto          su fila en el ranking por saldo
 * @param jugadores       total de jugadores del ranking, para mostrar "puesto X de N"
 * @param apuestasEnJuego número de apuestas activas
 */
public record PerfilJugador(PuestoRanking puesto, int jugadores, long apuestasEnJuego) {
}

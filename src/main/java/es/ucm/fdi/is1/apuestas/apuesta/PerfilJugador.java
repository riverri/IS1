package es.ucm.fdi.is1.apuestas.apuesta;

/** Lo que se ve de otro jugador (HU-48): su puesto, sus cifras y cuántas apuestas tiene en juego. */
public record PerfilJugador(PuestoRanking puesto, int jugadores, long apuestasEnJuego) {
}

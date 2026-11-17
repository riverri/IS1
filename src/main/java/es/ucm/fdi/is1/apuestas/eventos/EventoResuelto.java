package es.ucm.fdi.is1.apuestas.eventos;

/**
 * Un evento ha terminado (con resultado o marcador) o se ha anulado. Lo publica {@code apuesta.ResolucionService}
 * después de resolver las apuestas; lo escucha {@code ligas.RetoService} para resolver los retos (HU-89).
 */
public record EventoResuelto(Evento evento) {
}

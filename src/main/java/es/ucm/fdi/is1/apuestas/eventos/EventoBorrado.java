package es.ucm.fdi.is1.apuestas.eventos;

/**
 * Se va a borrar un evento creado por error (HU-46). Lo publica {@code gestion.GestionService} antes de borrarlo, en
 * la misma transacción: quien guarda datos del evento los borra (historial de cuotas, alertas, supercuotas) o, si
 * hay dinero en juego (retos), lanza una excepción y el evento no se borra.
 */
public record EventoBorrado(Evento evento) {
}

package es.ucm.fdi.is1.apuestas.eventos;

/**
 * Se ha dado de alta un evento, a mano o desde la API. Lo escucha {@code seguimiento.FavoritosService} para avisar a
 * quien sigue a alguno de sus equipos (HU-86).
 */
public record EventoCreado(Evento evento) {
}

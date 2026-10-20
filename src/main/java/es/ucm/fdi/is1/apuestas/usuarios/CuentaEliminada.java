package es.ucm.fdi.is1.apuestas.usuarios;

/**
 * Se publica al eliminar una cuenta (HU-18), antes de anonimizarla, para que otras partes limpien lo suyo.
 * Evento de Spring: lo publica {@code BajaService} y lo escucha {@code LigaService} con
 * {@code @EventListener} (borra las ligas que creó el usuario y lo saca de las demás).
 */
public record CuentaEliminada(Long usuarioId) {
}

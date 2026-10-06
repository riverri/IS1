package es.ucm.fdi.is1.apuestas.usuarios;

/** Se publica al eliminar una cuenta (HU-18), antes de anonimizarla, para que otras partes limpien lo suyo. */
public record CuentaEliminada(Long usuarioId) {
}

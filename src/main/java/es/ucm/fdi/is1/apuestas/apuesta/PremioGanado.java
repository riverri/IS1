package es.ucm.fdi.is1.apuestas.apuesta;

/**
 * Se ha ganado una apuesta y el usuario quiere enterarse por correo (HU-77). Lo publica {@link AvisosApuestas}
 * dentro de la transacción que resuelve la apuesta y lo recoge {@link AvisoPorCorreo} cuando esa transacción se ha
 * guardado. Lleva copiados los datos que hacen falta para el correo: después de guardar ya no hay sesión de base de
 * datos abierta para leer la apuesta.
 *
 * @param texto el mismo texto que el aviso de la web
 */
public record PremioGanado(String email, String nombre, String texto) {
}

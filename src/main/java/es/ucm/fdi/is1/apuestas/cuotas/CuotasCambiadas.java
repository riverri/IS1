package es.ucm.fdi.is1.apuestas.cuotas;

/**
 * Han cambiado las cuotas 1X2 de un evento (HU-91). Lo publica {@link RegistroCuotas}; lo escucha
 * {@code seguimiento.AlertasService} para avisar a quien esperaba una cuota (HU-90).
 */
public record CuotasCambiadas(Long eventoId, Cuotas cuotas) {
}

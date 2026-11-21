package es.ucm.fdi.is1.apuestas.ligas;

/**
 * Se va a borrar una liga (la borra quien la creó, o se elimina su cuenta). Lo publica {@link LigaService} justo antes
 * de borrarla, dentro de la misma transacción, para que lo que apunta a ella se quite antes: los bloqueos de liga
 * ({@link BloqueoLigaService#alBorrarseLiga}).
 */
public record LigaBorrada(Liga liga) {
}

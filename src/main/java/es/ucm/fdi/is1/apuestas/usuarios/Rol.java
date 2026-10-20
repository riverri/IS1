package es.ucm.fdi.is1.apuestas.usuarios;

/**
 * Rol de una cuenta. Spring Security lo recibe como {@code ROLE_USUARIO} o {@code ROLE_CREADOR}
 * y {@code SeguridadConfig} decide con él qué páginas puede abrir cada uno.
 */
public enum Rol {
    /** Jugador: apuesta con sus moneditas. */
    USUARIO,
    /** Creador de apuestas: además entra en el panel de gestión ({@code /gestion}). */
    CREADOR
}

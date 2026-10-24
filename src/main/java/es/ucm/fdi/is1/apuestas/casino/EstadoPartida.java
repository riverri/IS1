package es.ucm.fdi.is1.apuestas.casino;

/** Estado de una {@link Partida}: la ruleta termina al girar; el blackjack sigue en curso hasta que se resuelve la mano. */
public enum EstadoPartida {
    EN_CURSO,
    TERMINADA
}

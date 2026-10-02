package es.ucm.fdi.is1.apuestas.apuesta;

/** La apuesta supera los límites que se ha puesto el usuario o tiene una pausa activa (HU-10). */
public class JuegoResponsableException extends IllegalArgumentException {

    public JuegoResponsableException(String mensaje) {
        super(mensaje);
    }
}

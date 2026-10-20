package es.ucm.fdi.is1.apuestas.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que ha cambiado en una sincronización, para mostrarlo al creador de apuestas.
 * La rellena {@link SincronizacionService} mientras sincroniza: los métodos que cuentan son de paquete para que
 * solo pueda hacerlo él. En Gestión se muestra como mensaje y los errores aparte (HU-21).
 */
public class ResumenSincronizacion {

    private int eventosNuevos;
    private int eventosActualizados;
    private int resultados;
    private int equiposNuevos;
    private final List<String> errores = new ArrayList<>();

    void eventoNuevo() {
        eventosNuevos++;
    }

    void eventoActualizado() {
        eventosActualizados++;
    }

    void resultado() {
        resultados++;
    }

    void equipoNuevo() {
        equiposNuevos++;
    }

    /** Fallo en un partido o en una competición; la sincronización sigue con el resto. */
    void error(String mensaje) {
        errores.add(mensaje);
    }

    public int getEventosNuevos() {
        return eventosNuevos;
    }

    public int getEventosActualizados() {
        return eventosActualizados;
    }

    public int getResultados() {
        return resultados;
    }

    public int getEquiposNuevos() {
        return equiposNuevos;
    }

    public List<String> getErrores() {
        return errores;
    }

    /** Texto del mensaje de Gestión y del registro: "2 eventos nuevos, 1 actualizados, …". */
    @Override
    public String toString() {
        return eventosNuevos + " eventos nuevos, " + eventosActualizados + " actualizados, " + resultados
                + " resultados, " + equiposNuevos + " equipos nuevos" + (errores.isEmpty() ? "" : ", "
                + errores.size() + " errores");
    }
}

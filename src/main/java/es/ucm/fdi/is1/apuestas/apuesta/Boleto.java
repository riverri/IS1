package es.ucm.fdi.is1.apuestas.apuesta;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

/**
 * Boleto en construcción del usuario (HU-28): las selecciones que va eligiendo en el catálogo
 * antes de confirmar. Vive en la sesión; no se guarda en la base de datos hasta confirmar.
 *
 * <p>Con {@code @SessionScope} Spring crea un boleto por sesión HTTP. En {@link BoletoController} se inyecta
 * un proxy que, en cada petición, apunta al boleto de la sesión actual. Es {@link Serializable} para poder guardarse
 * con la sesión. Las reglas (límites, cuotas, eventos disponibles) se aplican en {@link BoletoService};
 * aquí solo se impide repetir partido (CA-28.2).
 */
@Component
@SessionScope
public class Boleto implements Serializable {

    // Número de versión del formato al guardar el objeto (serializarlo); lo exige Serializable
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Una selección elegida, con la cuota que vio el usuario al añadirla: un resultado (1X2)
     * o un tipo especial (HU-52).
     */
    public record Linea(Long eventoId, Resultado resultado, Especial especial, BigDecimal cuotaVista)
            implements Serializable {

        /** Línea 1X2. */
        public Linea(Long eventoId, Resultado resultado, BigDecimal cuotaVista) {
            this(eventoId, resultado, null, cuotaVista);
        }

        /** "LOCAL", "EMPATE"… o "MAS_2_5", "AMBOS_SI"… */
        String opcion() {
            return especial != null ? especial.name() : resultado.name();
        }
    }

    private final List<Linea> lineas = new ArrayList<>();

    void anadir(Long eventoId, Resultado resultado, BigDecimal cuotaVista) {
        anadir(new Linea(eventoId, resultado, cuotaVista));
    }

    void anadir(Long eventoId, Especial especial, BigDecimal cuotaVista) {
        anadir(new Linea(eventoId, null, especial, cuotaVista));
    }

    /** Lanza IllegalArgumentException si ya hay una selección de ese partido (CA-28.2). */
    private void anadir(Linea linea) {
        if (contiene(linea.eventoId())) {
            throw new IllegalArgumentException("Ya tienes una selección de ese partido en el boleto");
        }
        lineas.add(linea);
    }

    void quitar(Long eventoId) {
        // Los Long se comparan con equals, no con == (== compararía si son el mismo objeto, no el mismo número)
        lineas.removeIf(l -> l.eventoId().equals(eventoId));
    }

    /** Cambia la cuota vista de la línea de ese evento; como Linea es un record, se sustituye por otra. */
    void actualizarCuota(Long eventoId, BigDecimal cuota) {
        lineas.replaceAll(l -> l.eventoId().equals(eventoId) ? new Linea(eventoId, l.resultado(), l.especial(), cuota) : l);
    }

    void vaciar() {
        lineas.clear();
    }

    public boolean contiene(Long eventoId) {
        return lineas.stream().anyMatch(l -> l.eventoId().equals(eventoId));
    }

    /**
     * Para marcar en el catálogo la cuota elegida: {@code ${boleto.contiene(ev.id, 'LOCAL')}}
     * o {@code ${boleto.contiene(ev.id, 'MAS_2_5')}}.
     */
    public boolean contiene(Long eventoId, String opcion) {
        return lineas.stream().anyMatch(l -> l.eventoId().equals(eventoId) && l.opcion().equals(opcion));
    }

    public List<Linea> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public int getTamano() {
        return lineas.size();
    }

    public boolean isVacio() {
        return lineas.isEmpty();
    }
}

package es.ucm.fdi.is1.apuestas.apuesta;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;

/**
 * Boleto en construcción del usuario (HU-28): las selecciones que va eligiendo en el catálogo
 * antes de confirmar. Vive en la sesión; no se guarda en la base de datos hasta confirmar.
 */
@Component
@SessionScope
public class Boleto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Una selección elegida, con la cuota que vio el usuario al añadirla. */
    public record Linea(Long eventoId, Resultado resultado, BigDecimal cuotaVista) implements Serializable {
    }

    private final List<Linea> lineas = new ArrayList<>();

    void anadir(Long eventoId, Resultado resultado, BigDecimal cuotaVista) {
        if (contiene(eventoId)) {
            throw new IllegalArgumentException("Ya tienes una selección de ese partido en el boleto");
        }
        lineas.add(new Linea(eventoId, resultado, cuotaVista));
    }

    void quitar(Long eventoId) {
        lineas.removeIf(l -> l.eventoId().equals(eventoId));
    }

    void actualizarCuota(Long eventoId, BigDecimal cuota) {
        lineas.replaceAll(l -> l.eventoId().equals(eventoId) ? new Linea(eventoId, l.resultado(), cuota) : l);
    }

    void vaciar() {
        lineas.clear();
    }

    public boolean contiene(Long eventoId) {
        return lineas.stream().anyMatch(l -> l.eventoId().equals(eventoId));
    }

    /** Para marcar en el catálogo la cuota elegida: {@code ${boleto.contiene(ev.id, 'LOCAL')}}. */
    public boolean contiene(Long eventoId, String resultado) {
        return lineas.stream().anyMatch(l -> l.eventoId().equals(eventoId) && l.resultado().name().equals(resultado));
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

package es.ucm.fdi.is1.apuestas.ligas;

import java.util.List;

/**
 * Alguien ha salido de una liga o se ha borrado la liga entera: estos usuarios han dejado de compartirla con
 * los demás. Lo publica {@link LigaService} y lo escucha {@link RetoService}, que devuelve los retos pendientes
 * entre quienes ya no comparten ninguna liga (si no, dejarían de verse en la web y nadie podría contestarlos).
 */
public record MiembrosLigaCambiados(List<Long> usuarios) {
}

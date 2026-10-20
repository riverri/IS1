package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.List;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/**
 * El boleto listo para mostrar: cada selección con su cuota actual y el multiplicador total (HU-29).
 * Lo construye {@link BoletoService} a partir del {@link Boleto} de la sesión; no se guarda.
 *
 * @param cuotaTotal producto de las cuotas actuales (CA-29.1)
 */
public record BoletoVista(List<LineaVista> lineas, BigDecimal cuotaTotal) {

    /**
     * Una selección con su cuota actual.
     *
     * @param cambiada true si la cuota actual no coincide con la que vio el usuario (CA-29.3)
     */
    public record LineaVista(Evento evento, Resultado resultado, Especial especial, BigDecimal cuota,
                             boolean cambiada) {

        /** Línea 1X2. */
        public LineaVista(Evento evento, Resultado resultado, BigDecimal cuota, boolean cambiada) {
            this(evento, resultado, null, cuota, cambiada);
        }

        /** El escudo que se muestra: el del visitante si se apuesta por él, si no el del local. */
        public Equipo getEquipo() {
            return resultado == Resultado.VISITANTE || especial == Especial.DOBLE_X2
                    ? evento.getVisitante() : evento.getLocal();
        }

        /** "1", "X", "2" o el del tipo especial ("1X", "+2,5"…). */
        public String getSimbolo() {
            return especial != null ? especial.getSimbolo() : resultado.getSimbolo();
        }

        /** Nombre del equipo elegido, "Empate" o la descripción del tipo especial (como en {@link Seleccion}). */
        public String getDescripcionPronostico() {
            if (especial != null) {
                return especial.getDescripcion();
            }
            return switch (resultado) {
                case LOCAL -> evento.getLocal().getNombre();
                case EMPATE -> "Empate";
                case VISITANTE -> evento.getVisitante().getNombre();
            };
        }
    }

    /** Con una sola línea el boleto es una apuesta simple (CA-28.3). */
    public boolean isCombinada() {
        return lineas.size() > 1;
    }

    /** Si hay que pedir al usuario que acepte las cuotas nuevas antes de confirmar (CA-29.3). */
    public boolean isAlgunaCambiada() {
        return lineas.stream().anyMatch(LineaVista::cambiada);
    }
}

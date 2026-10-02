package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.List;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** El boleto listo para mostrar: cada selección con su cuota actual y el multiplicador total (HU-29). */
public record BoletoVista(List<LineaVista> lineas, BigDecimal cuotaTotal) {

    public record LineaVista(Evento evento, Resultado resultado, Especial especial, BigDecimal cuota,
                             boolean cambiada) {

        public LineaVista(Evento evento, Resultado resultado, BigDecimal cuota, boolean cambiada) {
            this(evento, resultado, null, cuota, cambiada);
        }

        /** El escudo que se muestra: el del visitante si se apuesta por él, si no el del local. */
        public Equipo getEquipo() {
            return resultado == Resultado.VISITANTE || especial == Especial.DOBLE_X2
                    ? evento.getVisitante() : evento.getLocal();
        }

        public String getSimbolo() {
            return especial != null ? especial.getSimbolo() : resultado.getSimbolo();
        }

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

    public boolean isCombinada() {
        return lineas.size() > 1;
    }

    public boolean isAlgunaCambiada() {
        return lineas.stream().anyMatch(LineaVista::cambiada);
    }
}

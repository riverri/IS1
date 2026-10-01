package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.util.List;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** El boleto listo para mostrar: cada selección con su cuota actual y el multiplicador total (HU-29). */
public record BoletoVista(List<LineaVista> lineas, BigDecimal cuotaTotal) {

    public record LineaVista(Evento evento, Resultado resultado, BigDecimal cuota, boolean cambiada) {

        public String getDescripcionPronostico() {
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

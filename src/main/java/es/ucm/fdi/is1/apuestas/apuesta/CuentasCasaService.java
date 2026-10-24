package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;

/**
 * Panel de la casa (HU-54): cuánto se ha apostado y pagado, y el margen real frente al teórico.
 * Las apuestas canceladas no cuentan, porque se devolvió el importe antes de jugarse.
 */
@Service
public class CuentasCasaService {

    private final ApuestaRepository apuestas;

    public CuentasCasaService(ApuestaRepository apuestas) {
        this.apuestas = apuestas;
    }

    /** Calcula el panel completo: total, desglose por tipo (CA-54.2) y por deporte, y los márgenes. */
    @Transactional(readOnly = true)
    public CuentasCasa cuentas() {
        // Todos los estados menos CANCELADA (complementOf = "todos los valores del enum excepto estos")
        List<Apuesta> todas = apuestas.findByEstadoIn(EnumSet.complementOf(EnumSet.of(EstadoApuesta.CANCELADA)));

        // Tres grupos sin solapes: largo plazo, combinadas de partidos y simples de partidos
        List<Apuesta> largoPlazo = todas.stream().filter(Apuesta::isLargoPlazo).toList();
        List<Apuesta> combinadas = todas.stream().filter(a -> !a.isLargoPlazo() && a.isCombinada()).toList();
        List<Apuesta> simples = todas.stream().filter(a -> !a.isLargoPlazo() && !a.isCombinada()).toList();

        List<CuentasCasa.Fila> porTipo = List.of(
                CuentasCasa.Fila.de("Simples · 1X2", simples.stream()
                        .filter(a -> a.getSelecciones().get(0).getEspecial() == null).toList()),
                CuentasCasa.Fila.de("Simples · doble oportunidad y goles", simples.stream()
                        .filter(a -> a.getSelecciones().get(0).getEspecial() != null).toList()),
                CuentasCasa.Fila.de("Combinadas", combinadas),
                CuentasCasa.Fila.de("Largo plazo", largoPlazo));

        // Solo las simples: una combinada puede mezclar deportes. TreeMap ordena por el orden del enum Deporte
        Map<Deporte, List<Apuesta>> deportes = new TreeMap<>();
        for (Apuesta a : simples) {
            // computeIfAbsent: si el deporte aún no tiene lista, la crea; después añade la apuesta a esa lista
            deportes.computeIfAbsent(a.getSelecciones().get(0).getEvento().getDeporte(), d -> new ArrayList<>()).add(a);
        }
        List<CuentasCasa.Fila> porDeporte = deportes.entrySet().stream()
                .map(e -> CuentasCasa.Fila.de(e.getKey().getIcono() + " " + e.getKey().getNombre(), e.getValue()))
                .toList();

        // distinct() quita los repetidos: cuenta cada usuario una sola vez aunque tenga varias apuestas
        long jugadores = todas.stream().map(a -> a.getUsuario().getId()).distinct().count();
        // Margen teórico de una simple (1 − 1/margen de la casa), en % con un decimal
        BigDecimal margenTeorico = BigDecimal.valueOf(CalculadoraCuotas.margenTeorico() * 100)
                .setScale(1, RoundingMode.HALF_UP);
        return new CuentasCasa(CuentasCasa.Fila.de("Total", todas), porTipo, porDeporte, jugadores, margenTeorico);
    }
}

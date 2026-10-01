package es.ucm.fdi.is1.apuestas.cuotas;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import es.ucm.fdi.is1.apuestas.eventos.Evento;

/**
 * Algoritmo de cuotas, versión 1 (HU-03).
 *
 * <ol>
 *   <li>Diferencia de nivel = (calidad local + ventaja de jugar en casa) - calidad visitante.</li>
 *   <li>Probabilidad de empate: alta si los equipos están igualados y baja si no (solo fútbol).</li>
 *   <li>El resto se reparte entre local y visitante con una curva logística según la diferencia.</li>
 *   <li>Se aplica el margen de la casa y se redondea hacia abajo, así la suma de 1/cuota es siempre mayor que 1.</li>
 * </ol>
 *
 * Pendiente para versiones posteriores: factor de forma (HU-02) y ajuste por volumen apostado.
 */
@Service
public class CalculadoraCuotas {

    static final double VENTAJA_LOCAL = 0.5;
    static final double PENDIENTE = 0.45;
    static final double EMPATE_MAXIMO = 0.30;
    static final double EMPATE_MINIMO = 0.10;
    static final double EMPATE_POR_PUNTO = 0.02;
    static final double MARGEN_CASA = 1.07;
    static final BigDecimal CUOTA_MINIMA = new BigDecimal("1.01");
    static final BigDecimal CUOTA_MAXIMA = new BigDecimal("50.00");

    public Cuotas calcular(Evento evento) {
        return calcular(evento.getLocal().getCalidad(), evento.getVisitante().getCalidad(),
                evento.getDeporte().isAdmiteEmpate());
    }

    public Cuotas calcular(double calidadLocal, double calidadVisitante, boolean admiteEmpate) {
        double diferencia = calidadLocal + VENTAJA_LOCAL - calidadVisitante;

        double probabilidadEmpate = 0;
        if (admiteEmpate) {
            probabilidadEmpate = Math.max(EMPATE_MINIMO, EMPATE_MAXIMO - EMPATE_POR_PUNTO * Math.abs(diferencia));
        }
        double repartoLocal = 1 / (1 + Math.exp(-PENDIENTE * diferencia));
        double probabilidadLocal = (1 - probabilidadEmpate) * repartoLocal;
        double probabilidadVisitante = (1 - probabilidadEmpate) * (1 - repartoLocal);

        return new Cuotas(
                cuota(probabilidadLocal),
                admiteEmpate ? cuota(probabilidadEmpate) : null,
                cuota(probabilidadVisitante));
    }

    /** Cuota = 1 / (probabilidad × margen), redondeada hacia abajo a 2 decimales y acotada. */
    private BigDecimal cuota(double probabilidad) {
        BigDecimal cuota = BigDecimal.valueOf(1 / (probabilidad * MARGEN_CASA)).setScale(2, RoundingMode.DOWN);
        return cuota.max(CUOTA_MINIMA).min(CUOTA_MAXIMA);
    }
}

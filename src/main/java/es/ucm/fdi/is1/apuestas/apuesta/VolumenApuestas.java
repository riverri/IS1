package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.cuotas.VolumenApostado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/**
 * Dinero de las apuestas activas en cada resultado, para el ajuste de cuotas por volumen.
 * <ul>
 *   <li>Una combinada reparte su importe entre sus selecciones (no pesa como si fuera una simple a cada una).</li>
 *   <li>Lo que aporta cada usuario a un resultado está limitado a {@value #APORTACION_MAXIMA} monedas. Como apostar
 *       y cancelar es gratis, sin este límite un solo usuario podía mover las cuotas a su favor (apostar mucho a
 *       un resultado, apostar al contrario con la cuota inflada y cancelar lo primero).</li>
 * </ul>
 *
 * <p>Implementa la interfaz {@link VolumenApostado} del paquete de cuotas: así
 * {@link es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas} obtiene el volumen sin depender de las apuestas.
 * Solo cuentan las selecciones 1X2: los otros tipos de apuesta no tienen ajuste por volumen.
 */
@Component
class VolumenApuestas implements VolumenApostado {

    /** Máximo que cuenta de un mismo usuario en un mismo resultado, en moneditas. */
    static final BigDecimal APORTACION_MAXIMA = new BigDecimal("100");

    private final SeleccionRepository selecciones;

    VolumenApuestas(SeleccionRepository selecciones) {
        this.selecciones = selecciones;
    }

    /** Importe que cuenta en cada resultado del evento; los resultados sin apuestas no aparecen en el mapa. */
    @Override
    public Map<Resultado, BigDecimal> importes(Evento evento) {
        Map<Resultado, BigDecimal> importes = new EnumMap<>(Resultado.class);
        // Un evento sin guardar (sin id) aún no puede tener apuestas
        if (evento.getId() == null) {
            return importes;
        }
        record Clave(Resultado resultado, Long usuario) {
        }
        Map<Clave, BigDecimal> porUsuario = new HashMap<>();
        // fila = [pronóstico, id del usuario, importe de la apuesta, número de selecciones]
        for (Object[] fila : selecciones.seleccionesActivasPorPronostico(evento)) {
            // Parte de la apuesta que corresponde a esta selección: importe / número de selecciones
            BigDecimal parte = ((BigDecimal) fila[2])
                    .divide(BigDecimal.valueOf(((Number) fila[3]).longValue()), MathContext.DECIMAL64);
            porUsuario.merge(new Clave((Resultado) fila[0], (Long) fila[1]), parte, BigDecimal::add);
        }
        // Primero se suma por usuario y resultado; después se aplica el tope y se suma por resultado
        porUsuario.forEach((clave, importe) ->
                importes.merge(clave.resultado(), importe.min(APORTACION_MAXIMA), BigDecimal::add));
        return importes;
    }
}

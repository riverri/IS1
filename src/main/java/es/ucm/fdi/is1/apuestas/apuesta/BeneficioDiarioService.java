package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldo;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.web.GraficoLinea;

/**
 * Beneficio de la casa en cada uno de los últimos días (HU-95). Sale del libro de movimientos del saldo: lo que los
 * jugadores pagan en los juegos contra la casa es ingreso, y lo que cobran (premios, devoluciones y
 * correcciones) es gasto. No cuentan la cripto, en la que el usuario compra a precio de mercado, ni las recargas.
 */
@Service
public class BeneficioDiarioService {

    /** Días que se enseñan, contando hoy. */
    public static final int DIAS = 30;

    /** Movimientos de los juegos contra la casa: apuestas, casino, predicciones, quiniela y bloqueos de liga. */
    static final Set<TipoMovimiento> DE_LA_CASA = EnumSet.of(TipoMovimiento.APUESTA, TipoMovimiento.PREMIO,
            TipoMovimiento.DEVOLUCION, TipoMovimiento.CORRECCION, TipoMovimiento.COBRO_ANTICIPADO,
            TipoMovimiento.CASINO_APUESTA,
            TipoMovimiento.CASINO_PREMIO, TipoMovimiento.PREDICCION_COMPRA, TipoMovimiento.PREDICCION_VENTA,
            TipoMovimiento.PREDICCION_PAGO, TipoMovimiento.QUINIELA_BOLETO, TipoMovimiento.QUINIELA_PREMIO,
            TipoMovimiento.BLOQUEO_LIGA);

    private final MovimientoSaldoRepository movimientos;
    private final Clock reloj;

    public BeneficioDiarioService(MovimientoSaldoRepository movimientos, Clock reloj) {
        this.movimientos = movimientos;
        this.reloj = reloj;
    }

    /** Lo que gana (positivo) o pierde la casa un día. */
    public record Dia(LocalDate fecha, BigDecimal beneficio) {
    }

    /**
     * @param dias    los {@link #DIAS} días, del más antiguo a hoy; los días sin movimientos, a 0
     * @param grafico la línea del beneficio de cada día; null si no hay ningún movimiento
     */
    public record BeneficioDiario(List<Dia> dias, BigDecimal total, GraficoLinea grafico) {

        /** Los días con movimientos, del más reciente al más antiguo, para la tabla. */
        public List<Dia> getConMovimientos() {
            List<Dia> con = new ArrayList<>(dias.stream().filter(d -> d.beneficio().signum() != 0).toList());
            java.util.Collections.reverse(con);
            return con;
        }
    }

    @Transactional(readOnly = true)
    public BeneficioDiario ultimosDias() {
        LocalDate hoy = LocalDate.now(reloj);
        LocalDate desde = hoy.minusDays(DIAS - 1);
        Map<LocalDate, BigDecimal> porDia = new TreeMap<>();
        for (LocalDate d = desde; !d.isAfter(hoy); d = d.plusDays(1)) {
            porDia.put(d, BigDecimal.ZERO);
        }
        for (MovimientoSaldo m : movimientos.findByFechaGreaterThanEqualAndTipoIn(desde.atStartOfDay(), DE_LA_CASA)) {
            // El movimiento es del jugador: lo que él pierde lo gana la casa, y al revés
            porDia.computeIfPresent(m.getFecha().toLocalDate(), (d, b) -> b.subtract(m.getImporte()));
        }
        List<Dia> dias = porDia.entrySet().stream().map(e -> new Dia(e.getKey(), e.getValue())).toList();
        BigDecimal total = dias.stream().map(Dia::beneficio).reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean hayMovimientos = dias.stream().anyMatch(d -> d.beneficio().signum() != 0);
        GraficoLinea grafico = hayMovimientos
                ? GraficoLinea.de(dias.stream().map(d -> d.beneficio().doubleValue()).toList(), null, null) : null;
        return new BeneficioDiario(dias, total, grafico);
    }
}

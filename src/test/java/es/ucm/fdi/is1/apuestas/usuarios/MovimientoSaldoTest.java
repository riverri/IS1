package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/** HU-55: cada cambio del saldo de un usuario deja un movimiento con el saldo resultante (sin base de datos). */
class MovimientoSaldoTest {

    private static final LocalDateTime ALTA = LocalDateTime.of(2026, 10, 1, 12, 0);

    private Usuario nuevoUsuario() {
        return new Usuario("ana@ucm.es", "Ana", "hash", Rol.USUARIO, new BigDecimal("1000"), ALTA);
    }

    @Test
    void elSaldoDeBienvenidaEsElPrimerMovimiento() {
        List<MovimientoSaldo> movimientos = nuevoUsuario().movimientosNuevos();

        assertThat(movimientos).hasSize(1);
        assertThat(movimientos.get(0).getTipo()).isEqualTo(TipoMovimiento.BIENVENIDA);
        assertThat(movimientos.get(0).getImporte()).isEqualByComparingTo("1000");
        assertThat(movimientos.get(0).getSaldoResultante()).isEqualByComparingTo("1000");
        assertThat(movimientos.get(0).getFecha()).isEqualTo(ALTA);
    }

    @Test
    void cadaCambioApuntaSuImporteConSignoYElSaldoQueQueda() {
        Usuario usuario = nuevoUsuario();

        usuario.cargar(new BigDecimal("10"), TipoMovimiento.APUESTA, "Nueva apuesta", ALTA.plusHours(1));
        usuario.abonar(new BigDecimal("18.50"), TipoMovimiento.PREMIO, "Premio", ALTA.plusHours(3));
        usuario.ajustar(new BigDecimal("-18.50"), TipoMovimiento.CORRECCION, "Corrección", ALTA.plusHours(4));
        usuario.aplicarRecargaPeriodica(ALTA.plusDays(7), Duration.ofDays(7), new BigDecimal("200"));

        List<MovimientoSaldo> movimientos = usuario.movimientosNuevos();
        assertThat(movimientos).extracting(MovimientoSaldo::getTipo).containsExactly(TipoMovimiento.BIENVENIDA,
                TipoMovimiento.APUESTA, TipoMovimiento.PREMIO, TipoMovimiento.CORRECCION, TipoMovimiento.RECARGA);
        assertThat(movimientos.get(1).getImporte()).isEqualByComparingTo("-10");
        assertThat(movimientos.get(1).getSaldoResultante()).isEqualByComparingTo("990");
        // CA-55.2: el último saldo resultante es el saldo, y la suma de los importes también
        assertThat(movimientos.getLast().getSaldoResultante()).isEqualByComparingTo(usuario.getSaldo());
        assertThat(movimientos.stream().map(MovimientoSaldo::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo(usuario.getSaldo());
    }

    @Test
    void sinSaldoNoSeCobraNiQuedaMovimiento() {
        Usuario usuario = nuevoUsuario();

        assertThatThrownBy(() -> usuario.cargar(new BigDecimal("1000.01"), TipoMovimiento.APUESTA, "Nueva apuesta",
                ALTA)).isInstanceOf(SaldoInsuficienteException.class);

        assertThat(usuario.getSaldo()).isEqualByComparingTo("1000");
        assertThat(usuario.movimientosNuevos()).hasSize(1);
    }

    @Test
    void laBajaDejaElSaldoACeroConSuMovimientoYDespuesNoEntraNada() {
        Usuario usuario = nuevoUsuario();

        usuario.eliminar("hash-inutilizable", ALTA.plusDays(1));
        usuario.abonar(new BigDecimal("50"), TipoMovimiento.PREMIO, "Premio", ALTA.plusDays(2));

        assertThat(usuario.getSaldo()).isEqualByComparingTo("0");
        assertThat(usuario.movimientosNuevos().getLast().getTipo()).isEqualTo(TipoMovimiento.BAJA);
        assertThat(usuario.movimientosNuevos().getLast().getImporte()).isEqualByComparingTo("-1000");
    }

    @Test
    void unConceptoDemasiadoLargoSeCorta() {
        Usuario usuario = nuevoUsuario();

        usuario.abonar(BigDecimal.ONE, TipoMovimiento.PREMIO, "x".repeat(300), ALTA);

        String concepto = usuario.movimientosNuevos().getLast().getConcepto();
        assertThat(concepto).hasSize(MovimientoSaldo.LARGO_CONCEPTO).endsWith("…");
    }
}

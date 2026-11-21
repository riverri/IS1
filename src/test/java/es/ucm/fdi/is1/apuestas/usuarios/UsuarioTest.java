package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Saldo y cuenta de un usuario sin arrancar Spring: recarga periódica (HU-14), cargos y abonos, límites y pausa
 * (juego responsable) y eliminar la cuenta. */
class UsuarioTest {

    private static final LocalDateTime ALTA = LocalDateTime.of(2026, 10, 1, 12, 0);
    private static final Duration SEMANA = Duration.ofDays(7);
    private static final BigDecimal RECARGA = new BigDecimal("200");

    private Usuario nuevoUsuario() {
        return new Usuario("ana@ucm.es", "Ana", "hash", Rol.USUARIO, new BigDecimal("1000"), ALTA);
    }

    @Test
    void empiezaConElSaldoDeBienvenida() {
        assertThat(nuevoUsuario().getSaldo()).isEqualByComparingTo("1000");
    }

    @Test
    void noRecargaAntesDeQuePaseElPeriodo() {
        Usuario usuario = nuevoUsuario();

        boolean recargado = usuario.aplicarRecargaPeriodica(ALTA.plusDays(6), SEMANA, RECARGA);

        assertThat(recargado).isFalse();
        assertThat(usuario.getSaldo()).isEqualByComparingTo("1000");
    }

    @Test
    void recargaCuandoHaPasadoElPeriodo() {
        Usuario usuario = nuevoUsuario();

        boolean recargado = usuario.aplicarRecargaPeriodica(ALTA.plusDays(7), SEMANA, RECARGA);

        assertThat(recargado).isTrue();
        assertThat(usuario.getSaldo()).isEqualByComparingTo("1200");
        assertThat(usuario.proximaRecarga(SEMANA)).isEqualTo(ALTA.plusDays(14));
    }

    @Test
    void soloRecargaUnaVezPorPeriodo() {
        Usuario usuario = nuevoUsuario();

        usuario.aplicarRecargaPeriodica(ALTA.plusDays(8), SEMANA, RECARGA);
        usuario.aplicarRecargaPeriodica(ALTA.plusDays(9), SEMANA, RECARGA);

        assertThat(usuario.getSaldo()).isEqualByComparingTo("1200");
    }

    private static final LocalDateTime LUEGO = ALTA.plusDays(1);

    @Test
    void laBienvenidaEsElPrimerMovimiento() {
        Usuario usuario = nuevoUsuario();
        assertThat(usuario.movimientosNuevos()).singleElement().satisfies(m -> {
            assertThat(m.getTipo()).isEqualTo(TipoMovimiento.BIENVENIDA);
            assertThat(m.getImporte()).isEqualByComparingTo("1000");
            assertThat(m.getSaldoResultante()).isEqualByComparingTo("1000");
            assertThat(m.getFecha()).isEqualTo(ALTA);
        });
        assertThat(usuario.getUltimaRecarga()).isEqualTo(ALTA);
    }

    @Test
    void laRecargaQuedaApuntadaYElPeriodoCuentaDesdeElDiaQueEntra() {
        Usuario usuario = nuevoUsuario();
        usuario.aplicarRecargaPeriodica(ALTA.plusDays(30), SEMANA, RECARGA);
        assertThat(usuario.getSaldo()).as("una sola recarga aunque pasaran cuatro semanas").isEqualByComparingTo("1200");
        assertThat(usuario.getUltimaRecarga()).isEqualTo(ALTA.plusDays(30));
        assertThat(usuario.movimientosNuevos()).last().satisfies(m -> {
            assertThat(m.getTipo()).isEqualTo(TipoMovimiento.RECARGA);
            assertThat(m.getSaldoResultante()).isEqualByComparingTo("1200");
        });
    }

    @Test
    void cargarRestaYApuntaElMovimiento() {
        Usuario usuario = nuevoUsuario();
        usuario.cargar(new BigDecimal("250.50"), TipoMovimiento.APUESTA, "Apuesta", LUEGO);
        assertThat(usuario.getSaldo()).isEqualByComparingTo("749.50");
        assertThat(usuario.movimientosNuevos()).hasSize(2).last().satisfies(m -> {
            assertThat(m.getImporte()).isEqualByComparingTo("-250.50");
            assertThat(m.getConcepto()).isEqualTo("Apuesta");
        });
    }

    @Test
    void sePuedeCargarTodoElSaldoPeroNiUnCentimoMas() {
        Usuario usuario = nuevoUsuario();
        assertThatThrownBy(() -> usuario.cargar(new BigDecimal("1000.01"), TipoMovimiento.APUESTA, "Apuesta", LUEGO))
                .isInstanceOf(SaldoInsuficienteException.class);
        assertThat(usuario.getSaldo()).isEqualByComparingTo("1000");
        usuario.cargar(new BigDecimal("1000"), TipoMovimiento.APUESTA, "Apuesta", LUEGO);
        assertThat(usuario.getSaldo()).isEqualByComparingTo("0");
    }

    @Test
    void unMovimientoDeCeroNoSeApunta() {
        Usuario usuario = nuevoUsuario();
        usuario.abonar(BigDecimal.ZERO, TipoMovimiento.PREMIO, "Premio", LUEGO);
        usuario.ajustar(BigDecimal.ZERO, TipoMovimiento.CORRECCION, "Corrección", LUEGO);
        assertThat(usuario.movimientosNuevos()).hasSize(1);
    }

    @Test
    void abonarYAjustarSuman() {
        Usuario usuario = nuevoUsuario();
        usuario.abonar(new BigDecimal("30"), TipoMovimiento.PREMIO, "Premio", LUEGO);
        usuario.ajustar(new BigDecimal("-1100"), TipoMovimiento.CORRECCION, "Corrección", LUEGO);
        assertThat(usuario.getSaldo()).as("una corrección puede dejar el saldo negativo").isEqualByComparingTo("-70");
    }

    @Test
    void eliminarDejaElSaldoACeroYYaNoRecibeNada() {
        Usuario usuario = nuevoUsuario();
        usuario.fijarLimites(new BigDecimal("10"), new BigDecimal("50"));
        usuario.pausarHasta(LUEGO.plusDays(3));
        usuario.eliminar("inutilizable", LUEGO);

        assertThat(usuario.isEliminado()).isTrue();
        assertThat(usuario.getEmail()).endsWith(Usuario.DOMINIO_ELIMINADOS).startsWith("eliminado-");
        assertThat(usuario.getNombre()).isEqualTo("Usuario eliminado");
        assertThat(usuario.getPasswordHash()).isEqualTo("inutilizable");
        assertThat(usuario.getSaldo()).isEqualByComparingTo("0");
        assertThat(usuario.movimientosNuevos()).last().satisfies(m -> {
            assertThat(m.getTipo()).isEqualTo(TipoMovimiento.BAJA);
            assertThat(m.getImporte()).isEqualByComparingTo("-1000");
        });
        assertThat(usuario.getLimiteDiario()).isNull();
        assertThat(usuario.getLimiteSemanal()).isNull();
        assertThat(usuario.getPausaHasta()).isNull();
        assertThat(usuario.enPausa(LUEGO)).isFalse();

        usuario.abonar(new BigDecimal("40"), TipoMovimiento.PREMIO, "Premio", LUEGO);
        usuario.ajustar(new BigDecimal("40"), TipoMovimiento.CORRECCION, "Corrección", LUEGO);
        assertThat(usuario.getSaldo()).isEqualByComparingTo("0");
    }

    @Test
    void eliminarConSaldoNegativoTambienLoDejaACero() {
        Usuario usuario = nuevoUsuario();
        usuario.ajustar(new BigDecimal("-1200"), TipoMovimiento.CORRECCION, "Corrección", LUEGO);
        usuario.eliminar("inutilizable", LUEGO);
        assertThat(usuario.getSaldo()).isEqualByComparingTo("0");
        assertThat(usuario.movimientosNuevos()).last().satisfies(m -> assertThat(m.getImporte()).isEqualByComparingTo("200"));
    }

    @Test
    void laPausaSoloSeAlargaYTerminaASuHora() {
        Usuario usuario = nuevoUsuario();
        assertThat(usuario.enPausa(LUEGO)).isFalse();
        usuario.pausarHasta(LUEGO.plusDays(7));
        usuario.pausarHasta(LUEGO.plusDays(2));
        assertThat(usuario.getPausaHasta()).as("no se acorta").isEqualTo(LUEGO.plusDays(7));
        usuario.pausarHasta(LUEGO.plusDays(9));
        assertThat(usuario.getPausaHasta()).isEqualTo(LUEGO.plusDays(9));
        assertThat(usuario.enPausa(LUEGO.plusDays(9).minusSeconds(1))).isTrue();
        assertThat(usuario.enPausa(LUEGO.plusDays(9))).isFalse();
    }

    @Test
    void datosDeLaCuenta() {
        Usuario usuario = nuevoUsuario();
        assertThat(usuario.isCreador()).isFalse();
        assertThat(new Usuario("c@ucm.es", "C", "h", Rol.CREADOR, BigDecimal.ZERO, ALTA).isCreador()).isTrue();
        usuario.cambiarNombre("Ana María");
        usuario.cambiarPasswordHash("otro");
        usuario.fijarLimites(new BigDecimal("20"), null);
        usuario.activarCorreos(false);
        usuario.activarResumen(true);
        assertThat(usuario.getNombre()).isEqualTo("Ana María");
        assertThat(usuario.getPasswordHash()).isEqualTo("otro");
        assertThat(usuario.getLimiteDiario()).isEqualByComparingTo("20");
        assertThat(usuario.getLimiteSemanal()).isNull();
        assertThat(usuario.isCorreosActivados()).isFalse();
        assertThat(usuario.isResumenSemanal()).isTrue();
        assertThat(usuario.getRol()).isEqualTo(Rol.USUARIO);
    }
}

package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** HU-14: recarga gratuita periódica. */
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
}

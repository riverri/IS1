package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class EstadisticasTest {

    @Test
    void sinApuestasResueltasTodoACero() {
        Estadisticas e = Estadisticas.de(List.of());

        assertThat(e.isTieneDatos()).isFalse();
        assertThat(e.getPorcentajeAciertos()).isEqualByComparingTo("0");
        assertThat(e.getRentabilidad()).isEqualByComparingTo("0");
    }

    @Test
    void calculaAciertosYRentabilidad() {
        Estadisticas e = new Estadisticas(4, 1, new BigDecimal("100"), new BigDecimal("-25"));

        assertThat(e.getPorcentajeAciertos()).isEqualByComparingTo("25.0");
        assertThat(e.getRentabilidad()).isEqualByComparingTo("-25.0");
    }
}

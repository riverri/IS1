package es.ucm.fdi.is1.apuestas.cripto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Reglas de la cartera sin base de datos (HU-71, HU-72): coste medio, valor y ganancia. */
class PosicionCriptoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    private static Criptomoneda doge(String precio) {
        Criptomoneda doge = new Criptomoneda("DOGE", "Dogecoin", "dogecoin", CategoriaCripto.MEMECOIN, 1);
        doge.actualizarPrecio(new BigDecimal(precio), null, AHORA);
        return doge;
    }

    @Test
    void elValorSigueAlPrecioYLaGananciaEsValorMenosCoste() {
        Criptomoneda doge = doge("0.20");
        PosicionCripto posicion = new PosicionCripto(null, doge);
        posicion.sumar(new BigDecimal("495"), new BigDecimal("100.00"));

        assertThat(posicion.getValor()).isEqualByComparingTo("99.00");
        assertThat(posicion.getGanancia()).isEqualByComparingTo("-1.00");

        // La memecoin dobla su precio
        doge.actualizarPrecio(new BigDecimal("0.40"), null, AHORA);
        assertThat(posicion.getValor()).isEqualByComparingTo("198.00");
        assertThat(posicion.getGanancia()).isEqualByComparingTo("98.00");
        assertThat(posicion.getRentabilidad()).isEqualByComparingTo("98.0");
    }

    @Test
    void venderUnaParteBajaElCosteEnProporcionYVenderloTodoLoDejaACero() {
        PosicionCripto posicion = new PosicionCripto(null, doge("0.20"));
        posicion.sumar(new BigDecimal("400"), new BigDecimal("80.00"));

        posicion.restar(new BigDecimal("100"));
        assertThat(posicion.getCantidad()).isEqualByComparingTo("300");
        assertThat(posicion.getCoste()).isEqualByComparingTo("60.00");

        posicion.restar(new BigDecimal("300"));
        assertThat(posicion.isConCantidad()).isFalse();
        assertThat(posicion.getCoste()).isEqualByComparingTo("0");
    }

    @Test
    void unPrecioSoloSirveDuranteLosMinutosConfigurados() {
        Criptomoneda doge = doge("0.20");
        assertThat(doge.precioAlDia(AHORA.plusMinutes(15), 15)).isTrue();
        assertThat(doge.precioAlDia(AHORA.plusMinutes(16), 15)).isFalse();
        assertThat(new Criptomoneda("SOL", "Solana", "solana", CategoriaCripto.REFERENCIA, 1)
                .precioAlDia(AHORA, 15)).isFalse();
    }

    @Test
    void lasMemecoinsVanPrimeroYAvisanDeRiesgoMuyAlto() {
        assertThat(CategoriaCripto.values()[0]).isEqualTo(CategoriaCripto.MEMECOIN);
        assertThat(CategoriaCripto.MEMECOIN.getRiesgo()).startsWith("Riesgo muy alto");
    }
}

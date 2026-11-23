package es.ucm.fdi.is1.apuestas.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/** La caché sin Spring: cuándo guarda, cuándo sirve lo guardado y cuándo vuelve a calcular. */
class CacheDeConsultaTest {

    private final AtomicInteger calculos = new AtomicInteger();

    private CacheDeConsulta<String, Integer> cache(Duration duracion, boolean sesionSinCambios) {
        return new CacheDeConsulta<>(List.of(String.class), duracion, () -> sesionSinCambios);
    }

    private Integer calcular() {
        return calculos.incrementAndGet();
    }

    @Test
    void guardaElResultadoHastaQueCambiaUnaEntidadDeLaQueDepende() {
        var cache = cache(Duration.ofMinutes(1), true);
        assertThat(cache.obtener("a", this::calcular)).isEqualTo(1);
        assertThat(cache.obtener("a", this::calcular)).isEqualTo(1);
        assertThat(cache.obtener("b", this::calcular)).as("otra clave se calcula aparte").isEqualTo(2);

        cache.cambio();
        assertThat(cache.obtener("a", this::calcular)).isEqualTo(3);
        assertThat(cache.dependeDe("texto")).isTrue();
        assertThat(cache.dependeDe(42)).isFalse();
    }

    @Test
    void noGuardaLoCalculadoMientrasCambiabanLosDatos() {
        var cache = cache(Duration.ofMinutes(1), true);
        // Un cambio que llega mientras se calcula: lo calculado puede ser viejo y no se guarda
        assertThat(cache.obtener("a", () -> {
            cache.cambio();
            return calcular();
        })).isEqualTo(1);
        assertThat(cache.obtener("a", this::calcular)).isEqualTo(2);
        assertThat(cache.obtener("a", this::calcular)).isEqualTo(2);
    }

    @Test
    void caducaYNoSeUsaConCambiosSinEscribir() {
        var caducada = cache(Duration.ZERO, true);
        caducada.obtener("a", this::calcular);
        assertThat(caducada.obtener("a", this::calcular)).isEqualTo(2);

        var conCambios = cache(Duration.ofMinutes(1), false);
        conCambios.obtener("a", this::calcular);
        assertThat(conCambios.obtener("a", this::calcular)).isEqualTo(4);
    }

    @Test
    void invalidarOlvidaLoGuardado() {
        var cache = cache(Duration.ofMinutes(1), true);
        cache.obtener("a", this::calcular);
        cache.invalidar();
        assertThat(cache.obtener("a", this::calcular)).isEqualTo(2);
    }
}

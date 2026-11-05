package es.ucm.fdi.is1.apuestas.anuncios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

/** HU-60 y HU-61: dónde se puede mostrar cada anuncio y cuál se elige (sin base de datos). */
class AnuncioTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 23, 12, 0);

    private static Anuncio anuncio(String titulo, String destino, int prioridad, Ubicacion... donde) {
        return new Anuncio(titulo, "Texto", null, destino, Set.of(donde), prioridad, null, null);
    }

    @Test
    void cadaRutaPerteneceASuSeccion() {
        assertThat(Ubicacion.de("/")).contains(Ubicacion.PORTADA);
        assertThat(Ubicacion.de("/eventos?deporte=FUTBOL")).contains(Ubicacion.EVENTOS);
        assertThat(Ubicacion.de("/eventos/7/apostar")).contains(Ubicacion.EVENTOS);
        assertThat(Ubicacion.de("/cuenta/movimientos")).contains(Ubicacion.CUENTA);
        assertThat(Ubicacion.de("/gestion")).isEmpty();
        assertThat(Ubicacion.de("/eventoss")).isEmpty();
        assertThat(Ubicacion.de("https://otra-web.com/mercados")).isEmpty();
        assertThat(Ubicacion.de("//otra-web.com")).isEmpty();
    }

    @Test
    void soloLlevaASeccionesDeLaWeb() {
        // CA-61.2
        assertThatThrownBy(() -> anuncio("Fuera", "https://otra-web.com", 5, Ubicacion.PORTADA))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("sección de la web");
        assertThatThrownBy(() -> anuncio("Gestión", "/gestion", 5, Ubicacion.PORTADA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Anuncio("Sin sitio", "Texto", null, "/ranking", new HashSet<>(), 5, null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("al menos una sección");
        assertThatThrownBy(() -> new Anuncio("Fechas", "Texto", null, "/ranking", Set.of(Ubicacion.PORTADA), 5,
                AHORA, AHORA.minusDays(1))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nuncaSeMuestraElDeLaSeccionEnLaQueEstas() {
        // CA-60.2: el de /mercados está puesto también en MERCADOS, pero ahí no sale
        Anuncio mercados = anuncio("Largo plazo", "/mercados", 5, Ubicacion.EVENTOS, Ubicacion.MERCADOS);

        assertThat(mercados.seMuestra(Ubicacion.EVENTOS, false, AHORA)).isTrue();
        assertThat(mercados.seMuestra(Ubicacion.MERCADOS, false, AHORA)).isFalse();
        assertThat(mercados.seMuestra(Ubicacion.RANKING, false, AHORA)).isFalse();
    }

    @Test
    void losQueLlevanASeccionesConSesionNoSeEnsenanALosVisitantes() {
        // CA-60.3
        Anuncio ligas = anuncio("Ligas", "/ligas", 5, Ubicacion.PORTADA);

        assertThat(ligas.seMuestra(Ubicacion.PORTADA, false, AHORA)).isFalse();
        assertThat(ligas.seMuestra(Ubicacion.PORTADA, true, AHORA)).isTrue();
    }

    @Test
    void respetaLasFechasYSiEstaActivo() {
        // CA-61.1
        Anuncio futuro = new Anuncio("Futuro", "Texto", null, "/ranking", Set.of(Ubicacion.PORTADA), 5,
                AHORA.plusDays(1), null);
        Anuncio pasado = new Anuncio("Pasado", "Texto", null, "/ranking", Set.of(Ubicacion.PORTADA), 5,
                null, AHORA.minusDays(1));
        Anuncio desactivado = anuncio("Apagado", "/ranking", 5, Ubicacion.PORTADA);
        desactivado.activar(false);

        assertThat(futuro.seMuestra(Ubicacion.PORTADA, true, AHORA)).isFalse();
        assertThat(pasado.seMuestra(Ubicacion.PORTADA, true, AHORA)).isFalse();
        assertThat(desactivado.seMuestra(Ubicacion.PORTADA, true, AHORA)).isFalse();
    }

    @Test
    void seEligeUnoDeLosDePrioridadMasAltaYSeTurnan() {
        // CA-60.1
        Anuncio a = anuncio("A", "/mercados", 5, Ubicacion.EVENTOS);
        Anuncio b = anuncio("B", "/ranking", 5, Ubicacion.EVENTOS);
        Anuncio bajo = anuncio("Bajo", "/equipos", 2, Ubicacion.EVENTOS);
        SplittableRandom azar = new SplittableRandom(42);

        Set<Anuncio> salidos = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            salidos.add(AnuncioService.elegir(List.of(a, b, bajo), Ubicacion.EVENTOS, false, AHORA, azar).orElseThrow());
        }

        assertThat(salidos).containsExactlyInAnyOrder(a, b);
    }

    @Test
    void sinNingunoPosibleNoSaleNada() {
        Optional<Anuncio> elegido = AnuncioService.elegir(List.of(anuncio("A", "/mercados", 5, Ubicacion.EVENTOS)),
                Ubicacion.CUENTA, true, AHORA, new SplittableRandom(1));

        assertThat(elegido).isEmpty();
    }
}

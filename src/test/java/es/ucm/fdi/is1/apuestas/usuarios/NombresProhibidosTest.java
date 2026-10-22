package es.ucm.fdi.is1.apuestas.usuarios;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Issue #75: qué nombres se rechazan por ofensivos y cuáles no (CA-11.3, CA-47.3). */
class NombresProhibidosTest {

    @Test
    void rechazaPalabrasDeLaListaSinImportarMayusculasNiTildes() {
        assertThat(NombresProhibidos.contiene("PUTA")).isTrue();
        assertThat(NombresProhibidos.contiene("Imbécil Total")).isTrue();
        assertThat(NombresProhibidos.contiene("el maricón")).isTrue();
    }

    @Test
    void rechazaLetrasCambiadasPorNumerosOSimbolos() {
        assertThat(NombresProhibidos.contiene("p0ll4")).isTrue();
        assertThat(NombresProhibidos.contiene("$h1t")).isTrue();
    }

    @Test
    void lasPalabrasLargasSeBuscanDentroDeOtrasYConElNombreTodoJunto() {
        assertThat(NombresProhibidos.contiene("Cabronazo")).isTrue();
        assertThat(NombresProhibidos.contiene("gili pollas")).isTrue();
    }

    @Test
    void noRechazaNombresNormalesQueContienenUnaPalabraCorta() {
        assertThat(NombresProhibidos.contiene("Bárbara Dickens")).isFalse();
        assertThat(NombresProhibidos.contiene("Pollock")).isFalse();
        assertThat(NombresProhibidos.contiene("Carlos Martín-Salas")).isFalse();
        assertThat(NombresProhibidos.contiene("Jing Li")).isFalse();
    }

    @Test
    void unNombreVacioNoSeConsideraOfensivo() {
        assertThat(NombresProhibidos.contiene(null)).isFalse();
        assertThat(NombresProhibidos.contiene("  ")).isFalse();
    }
}

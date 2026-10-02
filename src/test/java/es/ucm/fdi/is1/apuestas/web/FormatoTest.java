package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FormatoTest {

    private final Formato formato = new Formato();

    @Test
    void usaLosColoresDelClubSiLosConoce() {
        assertThat(formato.colores("FC Barcelona")).containsExactly("#a50044", "#004d98", "#ffffff");
    }

    @Test
    void textoOscuroSobreFondoClaro() {
        String[] colores = formato.colores("Real Madrid");
        assertThat(colores[0]).isEqualTo("#ffffff");
        assertThat(colores[2]).isNotEqualTo("#ffffff");
    }

    @Test
    void equipoDesconocidoRecibeUnColorCalculado() {
        assertThat(formato.colores("Equipo Inventado")[0]).startsWith("hsl(");
    }

    @Test
    void iniciales() {
        assertThat(formato.iniciales("Real Madrid")).isEqualTo("M");
        assertThat(formato.iniciales("Racing de Santander")).isEqualTo("RS");
        assertThat(formato.iniciales("Carlos Alcaraz")).isEqualTo("CA");
    }

    @Test
    void apellidoParaElCampo() {
        assertThat(formato.apellido("Lamine Yamal")).isEqualTo("Yamal");
        assertThat(formato.apellido("Pedri")).isEqualTo("Pedri");
        assertThat(formato.apellido("  Frenkie de  Jong ")).isEqualTo("de Jong");
    }
}

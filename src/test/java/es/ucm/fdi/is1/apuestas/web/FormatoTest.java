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

    @Test
    void inicialesDeNombresSinLetrasAlPrincipio() {
        // Antes, "-Ana" y "/" lanzaban una excepción y el ranking daba un error 500 a todos los visitantes
        assertThat(formato.iniciales("-Ana")).isEqualTo("A");
        assertThat(formato.iniciales("/")).isEqualTo("?");
        assertThat(formato.iniciales(" - ")).isEqualTo("?");
    }

    @Test
    void fechasEnEspanol() {
        java.time.LocalDateTime sabado = java.time.LocalDateTime.of(2026, 10, 10, 21, 5);
        assertThat(formato.dia(sabado)).isEqualTo("Sáb 10 oct");
        assertThat(formato.hora(sabado)).isEqualTo("21:05");
        assertThat(formato.hora(sabado.withHour(9))).isEqualTo("09:05");
    }

    @Test
    void anclaSinTildesNiSignos() {
        assertThat(formato.ancla("Champions League")).isEqualTo("champions-league");
        assertThat(formato.ancla("  Liga Española (Grupo 1) ")).isEqualTo("liga-espanola-grupo-1");
        assertThat(formato.ancla("Copa-del-Rey")).isEqualTo("copa-del-rey");
    }

    @Test
    void cuotaConComaYCortadaSinRedondear() {
        assertThat(formato.cuota(new java.math.BigDecimal("1.859"))).isEqualTo("1,85");
        assertThat(formato.cuota(new java.math.BigDecimal("2"))).isEqualTo("2,00");
        assertThat(formato.cuota(null)).isEqualTo("–");
    }

    @Test
    void precioDeCriptoSegunSuTamano() {
        assertThat(formato.precioCripto(new java.math.BigDecimal("84312.554"))).isEqualTo("84.312,55");
        assertThat(formato.precioCripto(java.math.BigDecimal.ONE)).isEqualTo("1,00");
        assertThat(formato.precioCripto(new java.math.BigDecimal("0.0000123456"))).isEqualTo("0,00001235");
        assertThat(formato.precioCripto(new java.math.BigDecimal("0.5000"))).isEqualTo("0,5");
        assertThat(formato.precioCripto(null)).isEqualTo("–");
        assertThat(formato.precioGrafico(1234.5)).isEqualTo("1.234,50");
    }

    @Test
    void cantidadDeCriptoSegunSuTamano() {
        assertThat(formato.cantidadCripto(new java.math.BigDecimal("1250000.5"))).isEqualTo("1.250.000,5");
        assertThat(formato.cantidadCripto(java.math.BigDecimal.ONE)).isEqualTo("1");
        assertThat(formato.cantidadCripto(new java.math.BigDecimal("0.001234567"))).isEqualTo("0,00123457");
        assertThat(formato.cantidadCripto(java.math.BigDecimal.ZERO)).isEqualTo("0");
        assertThat(formato.cantidadCripto(null)).isEqualTo("0");
    }

    @Test
    void variacionConSignoYUnDecimal() {
        assertThat(formato.variacion(new java.math.BigDecimal("12.34"))).isEqualTo("+12,3 %");
        assertThat(formato.variacion(new java.math.BigDecimal("-4.55"))).isEqualTo("-4,6 %");
        assertThat(formato.variacion(new java.math.BigDecimal("0.04"))).isEqualTo("0,0 %");
        assertThat(formato.variacion(null)).isEqualTo("–");
    }

    @Test
    void colorEstableEnLaRueda() {
        assertThat(formato.color("Getafe CF")).isEqualTo(formato.color("Getafe CF")).matches("hsl\\((\\d{1,3}) 55% 38%\\)");
        int tono = Math.floorMod("Getafe CF".hashCode(), 360);
        assertThat(formato.color("Getafe CF")).isEqualTo("hsl(" + tono + " 55% 38%)");
    }
}

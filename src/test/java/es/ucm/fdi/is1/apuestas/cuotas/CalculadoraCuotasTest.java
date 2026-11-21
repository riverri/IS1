package es.ucm.fdi.is1.apuestas.cuotas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.Forma;
import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** HU-02, HU-03 y fila 3: cuotas con margen para la casa, forma reciente y ajuste por volumen. */
class CalculadoraCuotasTest {

    private final CalculadoraCuotas calculadora = new CalculadoraCuotas(evento -> Map.of());

    @Test
    void elFavoritoTieneCuotaMasBaja() {
        Cuotas cuotas = calculadora.calcular(9.0, 5.0, true);

        assertThat(cuotas.local()).isLessThan(cuotas.visitante());
    }

    @Test
    void conEquiposIgualesJugarEnCasaEsVentaja() {
        Cuotas cuotas = calculadora.calcular(7.0, 7.0, true);

        assertThat(cuotas.local()).isLessThan(cuotas.visitante());
    }

    @Test
    void laCasaSiempreTieneMargen() {
        for (double local = 0; local <= 10; local += 0.5) {
            for (double visitante = 0; visitante <= 10; visitante += 0.5) {
                assertThat(calculadora.calcular(local, visitante, true).sumaProbabilidadesImplicitas())
                        .as("fútbol %s-%s", local, visitante).isGreaterThan(BigDecimal.ONE);
                assertThat(calculadora.calcular(local, visitante, false).sumaProbabilidadesImplicitas())
                        .as("sin empate %s-%s", local, visitante).isGreaterThan(BigDecimal.ONE);
            }
        }
    }

    @Test
    void ningunaCuotaBajaDelMinimo() {
        Cuotas cuotas = calculadora.calcular(10.0, 0.0, false);

        assertThat(cuotas.local()).isGreaterThanOrEqualTo(new BigDecimal("1.01"));
        assertThat(cuotas.visitante()).isLessThanOrEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void sinEmpateEnDeportesQueNoLoAdmiten() {
        Cuotas cuotas = calculadora.calcular(8.0, 7.0, false);

        assertThat(cuotas.empate()).isNull();
        assertThat(cuotas.de(Resultado.EMPATE)).isNull();
    }

    @Test
    void lasCuotasTienenDosDecimales() {
        Cuotas cuotas = calculadora.calcular(8.3, 6.1, true);

        assertThat(cuotas.local().scale()).isEqualTo(2);
        assertThat(cuotas.empate().scale()).isEqualTo(2);
        assertThat(cuotas.visitante().scale()).isEqualTo(2);
    }

    @Test
    void laBuenaFormaBajaLaCuota() {
        double nivelNormal = CalculadoraCuotas.nivel(7.0, 0);
        double nivelEnRacha = CalculadoraCuotas.nivel(7.0, 2);

        assertThat(nivelEnRacha).isGreaterThan(nivelNormal);
        assertThat(calculadora.calcular(nivelEnRacha, 7.0, true).local())
                .isLessThan(calculadora.calcular(nivelNormal, 7.0, true).local());
    }

    @Test
    void sinDineroApostadoLasCuotasNoCambian() {
        assertThat(calculadora.calcular(8.0, 6.0, true, Map.of())).isEqualTo(calculadora.calcular(8.0, 6.0, true));
    }

    @Test
    void elResultadoConMasDineroBajaDeCuotaYLosDemasSuben() {
        Cuotas sinVolumen = calculadora.calcular(7.0, 7.0, true);
        Cuotas conVolumen = calculadora.calcular(7.0, 7.0, true, Map.of(Resultado.LOCAL, new BigDecimal("2000")));

        assertThat(conVolumen.local()).isLessThan(sinVolumen.local());
        assertThat(conVolumen.empate()).isGreaterThan(sinVolumen.empate());
        assertThat(conVolumen.visitante()).isGreaterThan(sinVolumen.visitante());
    }

    @Test
    void elVolumenTieneUnPesoMaximo() {
        // Aunque todo el dinero vaya al local, el favorito claro sigue siendo el visitante
        Cuotas cuotas = calculadora.calcular(3.0, 9.0, false, Map.of(Resultado.LOCAL, new BigDecimal("1000000")));

        assertThat(cuotas.visitante()).isLessThan(cuotas.local());
    }

    @Test
    void conVolumenLaCasaSigueTeniendoMargen() {
        Map<Resultado, BigDecimal> apostado = Map.of(Resultado.LOCAL, new BigDecimal("500"),
                Resultado.EMPATE, new BigDecimal("30"), Resultado.VISITANTE, new BigDecimal("4000"));
        for (double local = 0; local <= 10; local += 1) {
            assertThat(calculadora.calcular(local, 10 - local, true, apostado).sumaProbabilidadesImplicitas())
                    .isGreaterThan(BigDecimal.ONE);
            assertThat(calculadora.calcular(local, 10 - local, false, apostado).sumaProbabilidadesImplicitas())
                    .isGreaterThan(BigDecimal.ONE);
        }
    }

    @Test
    void elDineroAlEmpateNoCuentaSiNoHayEmpate() {
        assertThat(calculadora.calcular(8.0, 7.0, false, Map.of(Resultado.EMPATE, new BigDecimal("5000"))))
                .isEqualTo(calculadora.calcular(8.0, 7.0, false));
    }

    // --- Cuotas de un evento (las que ven las páginas) y especiales de fútbol (HU-52) ---

    private static Evento partido(Deporte deporte, double local, double visitante) {
        return new Evento(new Competicion("Liga", deporte), new Equipo("A", deporte, local),
                new Equipo("B", deporte, visitante), LocalDateTime.of(2026, 10, 10, 21, 0));
    }

    private static Map<Especial, BigDecimal> especiales(String dobleUnoX, String dobleXDos, String dobleUnoDos,
                                                        String mas, String menos, String ambosSi, String ambosNo) {
        return Map.of(Especial.DOBLE_1X, new BigDecimal(dobleUnoX), Especial.DOBLE_X2, new BigDecimal(dobleXDos),
                Especial.DOBLE_12, new BigDecimal(dobleUnoDos), Especial.MAS_2_5, new BigDecimal(mas),
                Especial.MENOS_2_5, new BigDecimal(menos), Especial.AMBOS_SI, new BigDecimal(ambosSi),
                Especial.AMBOS_NO, new BigDecimal(ambosNo));
    }

    @Test
    void lasCuotasDeUnEventoSalenDelNivelDeSusEquipos() {
        Evento evento = partido(Deporte.FUTBOL, 7.0, 6.0);
        Cuotas cuotas = calculadora.calcular(evento);
        // Fijadas con la fórmula de docs/cuotas: si cambian, cambia lo que ve todo el mundo
        assertThat(cuotas.local()).isEqualByComparingTo("1.93");
        assertThat(cuotas.empate()).isEqualByComparingTo("3.46");
        assertThat(cuotas.visitante()).isEqualByComparingTo("3.79");
        assertThat(calculadora.cuota(evento, Resultado.VISITANTE, null)).isEqualByComparingTo("3.79");
        assertThat(calculadora.cuota(evento, null, null)).isNull();
    }

    @Test
    void laFormaDeCadaEquipoCuentaEnSuLado() {
        Evento evento = partido(Deporte.FUTBOL, 7.0, 6.0);
        evento.getVisitante().setForma(Forma.BUENA);
        Cuotas conForma = calculadora.calcular(evento);
        assertThat(conForma).isEqualTo(calculadora.calcular(7.0, 6.4, true));
        evento.getLocal().setForma(Forma.MALA);
        assertThat(calculadora.calcular(evento)).isEqualTo(calculadora.calcular(6.6, 6.4, true));
    }

    @Test
    void elVolumenDelEventoSeConsultaAlCalcular() {
        Map<Resultado, BigDecimal> mucho = Map.of(Resultado.VISITANTE, new BigDecimal("5000"));
        CalculadoraCuotas conVolumen = new CalculadoraCuotas(e -> mucho);
        Evento evento = partido(Deporte.FUTBOL, 7.0, 6.0);
        assertThat(conVolumen.calcular(evento)).isEqualTo(calculadora.calcular(7.0, 6.0, true, mucho));
        assertThat(conVolumen.calcular(evento).visitante()).isLessThan(calculadora.calcular(evento).visitante());
    }

    @Test
    void elCobroAnticipadoNoCuentaElDineroDelPropioUsuario() {
        Map<Resultado, BigDecimal> todos = Map.of(Resultado.LOCAL, new BigDecimal("5000"));
        VolumenApostado volumen = new VolumenApostado() {
            @Override
            public Map<Resultado, BigDecimal> importes(Evento evento) {
                return todos;
            }

            @Override
            public Map<Resultado, BigDecimal> importes(Evento evento, Long usuarioExcluido) {
                return usuarioExcluido == 7L ? Map.of() : todos;
            }
        };
        CalculadoraCuotas conVolumen = new CalculadoraCuotas(volumen);
        Evento evento = partido(Deporte.FUTBOL, 7.0, 6.0);
        assertThat(conVolumen.cuota(evento, Resultado.LOCAL, null, 7L)).isEqualByComparingTo("1.93");
        assertThat(conVolumen.cuota(evento, Resultado.LOCAL, null, 8L))
                .isEqualByComparingTo(conVolumen.cuota(evento, Resultado.LOCAL, null));
        assertThat(conVolumen.cuota(evento, null, null, 7L)).isNull();
        assertThat(conVolumen.cuota(evento, null, Especial.AMBOS_SI, 7L)).isEqualByComparingTo("1.78");
    }

    @Test
    void especialesDeFutbolFijados() {
        Evento evento = partido(Deporte.FUTBOL, 7.0, 6.0);
        assertThat(calculadora.especiales(evento)).isEqualTo(especiales("1.23", "1.81", "1.28", "1.91", "1.82", "1.78", "1.96"));
        assertThat(calculadora.especiales(5.0, 5.0)).isEqualTo(especiales("1.36", "1.54", "1.31", "1.93", "1.80", "1.76", "1.98"));
        // Visitante muy superior: la doble X2 se queda en la cuota mínima
        assertThat(calculadora.especiales(2.0, 9.0)).isEqualTo(especiales("4.40", "1.01", "1.12", "1.52", "2.41", "2.06", "1.70"));
        assertThat(calculadora.cuota(evento, null, Especial.MENOS_2_5)).isEqualByComparingTo("1.82");
        // La especial se busca aunque también venga un resultado
        assertThat(calculadora.cuota(evento, Resultado.LOCAL, Especial.DOBLE_X2)).isEqualByComparingTo("1.81");
    }

    @Test
    void sinEspecialesFueraDelFutbol() {
        Evento baloncesto = partido(Deporte.BALONCESTO, 7.0, 6.0);
        assertThat(calculadora.especiales(baloncesto)).isEmpty();
        assertThat(calculadora.cuota(baloncesto, null, Especial.AMBOS_SI)).isNull();
        assertThat(calculadora.calcular(baloncesto).empate()).isNull();
    }

    @Test
    void elMargenTeoricoEsElDeLaCasa() {
        assertThat(CalculadoraCuotas.margenTeorico()).isCloseTo(1 - 1 / 1.07, within(1e-12));
        assertThat(CalculadoraCuotas.nivel(7.0, 1)).isCloseTo(7.4, within(1e-12));
        assertThat(CalculadoraCuotas.nivel(7.0, -1)).isCloseTo(6.6, within(1e-12));
    }
}

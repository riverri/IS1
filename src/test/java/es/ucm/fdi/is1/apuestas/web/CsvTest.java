package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** HU-114: ficheros CSV para Excel en español, sin que un texto se pueda ejecutar como fórmula. */
class CsvTest {

    private static String linea(Object... valores) {
        // Sin la marca BOM del principio ni el salto de línea del final
        return new Csv().fila(valores).toString().substring(1).replace("\r\n", "");
    }

    @Test
    void puntoYComaYComaDecimal() {
        assertThat(linea("Ana", new BigDecimal("12.5"), LocalDateTime.of(2026, 10, 4, 21, 0), null))
                .isEqualTo("Ana;12,50;04/10/2026 21:00;");
        assertThat(new Csv().toString()).startsWith("﻿");
    }

    @Test
    void entreComillasSiLlevaSeparadorComillasOSaltos() {
        assertThat(linea("a;b", "di \"hola\"", "dos\nlíneas"))
                .isEqualTo("\"a;b\";\"di \"\"hola\"\"\";\"dos\nlíneas\"");
    }

    @Test
    void unTextoQueEmpiezaComoFormulaSeQuedaEnTexto() {
        assertThat(linea("=HYPERLINK(\"http://x\")")).isEqualTo("\"'=HYPERLINK(\"\"http://x\"\")\"");
        assertThat(linea("+34", "-x", "@SUMA", "\tcosa")).isEqualTo("'+34;'-x;'@SUMA;'\tcosa");
        assertThat(Csv.sinFormula("Real Madrid")).isEqualTo("Real Madrid");
        assertThat(Csv.sinFormula("")).isEmpty();
        assertThat(Csv.sinFormula("a=b")).isEqualTo("a=b");
    }

    @Test
    void losImportesNegativosSiguenSiendoNumeros() {
        assertThat(linea(new BigDecimal("-12.50"))).isEqualTo("-12,50");
    }
}

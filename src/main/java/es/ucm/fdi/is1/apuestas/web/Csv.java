package es.ucm.fdi.is1.apuestas.web;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Ficheros CSV para descargar (HU-114). Separador punto y coma y coma decimal, como los espera Excel en español.
 * Empiezan con la marca BOM para que Excel lea bien las tildes.
 */
public final class Csv {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final StringBuilder texto = new StringBuilder("﻿");

    /** Añade una fila; los valores null se dejan vacíos. */
    public Csv fila(Object... valores) {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                texto.append(';');
            }
            texto.append(campo(valores[i]));
        }
        texto.append("\r\n");
        return this;
    }

    public Csv filas(List<Object[]> filas) {
        filas.forEach(this::fila);
        return this;
    }

    /** Respuesta que el navegador descarga con ese nombre de fichero. */
    public ResponseEntity<byte[]> descarga(String nombreFichero) {
        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        cabeceras.setContentDisposition(ContentDisposition.attachment().filename(nombreFichero).build());
        return new ResponseEntity<>(texto.toString().getBytes(StandardCharsets.UTF_8), cabeceras,
                org.springframework.http.HttpStatus.OK);
    }

    @Override
    public String toString() {
        return texto.toString();
    }

    private static String campo(Object valor) {
        if (valor == null) {
            return "";
        }
        String s = switch (valor) {
            case BigDecimal numero -> numero.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',');
            case LocalDateTime fecha -> fecha.format(FECHA);
            default -> valor.toString();
        };
        // Entre comillas si lleva separador, comillas o saltos de línea; las comillas se doblan
        if (s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}

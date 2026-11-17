package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-93: descargar mis apuestas y mis movimientos en CSV. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExportacionWebTest {

    private static final String ANA = "ana-csv@ucm.es";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @BeforeEach
    void preparar() {
        usuarios.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        Evento partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("12.50"));
    }

    private String descargar(String ruta) throws Exception {
        byte[] cuerpo = mvc.perform(get(ruta).with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("attachment")))
                .andReturn().getResponse().getContentAsByteArray();
        return new String(cuerpo, StandardCharsets.UTF_8);
    }

    @Test
    void descargoMisApuestasConPuntoYComaYComaDecimal() throws Exception {
        String csv = descargar("/apuestas/exportar");

        String[] lineas = csv.split("\r\n");
        assertThat(lineas[0]).isEqualTo("﻿Fecha;Apuesta;Selecciones;Importe;Cuota;Estado;Cobrado");
        assertThat(lineas).hasSize(2);
        assertThat(lineas[1]).contains("Getafe CF").contains(";12,50;").endsWith(";activa;0,00");
    }

    @Test
    void descargoMisMovimientosConElSaldoQueDejaCadaUno() throws Exception {
        String csv = descargar("/cuenta/movimientos/exportar");

        assertThat(csv).startsWith("﻿Fecha;Tipo;Concepto;Importe;Saldo");
        assertThat(csv).contains(";Apuesta;").contains(";-12,50;");
    }

    @Test
    void sinSesionNoSeDescargaNada() throws Exception {
        mvc.perform(get("/apuestas/exportar")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/cuenta/movimientos/exportar")).andExpect(status().is3xxRedirection());
    }
}

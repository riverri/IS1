package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** HU-85: el ping para el keep-alive (CA-85.1) y la caché de la hoja de estilos (CA-85.3). */
@SpringBootTest
@AutoConfigureMockMvc
class RendimientoWebTest {

    /** La hoja de estilos con la huella de su contenido (32 caracteres hexadecimales) en el nombre. */
    private static final Pattern ESTILOS = Pattern.compile("/css/estilos-[0-9a-f]{32}\\.css");

    @Autowired
    private MockMvc mvc;

    @Test
    void elPingRespondeSinIniciarSesion() throws Exception {
        mvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void laHojaDeEstilosLlevaSuHuellaYElNavegadorLaGuardaUnAnio() throws Exception {
        String portada = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();
        Matcher enlace = ESTILOS.matcher(portada);
        assertThat(enlace.find()).as("la portada enlaza /css/estilos-<huella>.css").isTrue();

        mvc.perform(get(enlace.group()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=31536000, public"));
    }
}

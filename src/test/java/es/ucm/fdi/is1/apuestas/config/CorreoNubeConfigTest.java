package es.ucm.fdi.is1.apuestas.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CorreoNubeConfigTest {

    @Test
    void rechazaUnDespliegueSinCredencialesSmtp() {
        assertThatThrownBy(() -> new CorreoNubeConfig("", "2525", "", "", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MAIL_HOST")
                .hasMessageContaining("MAIL_PASSWORD")
                .hasMessageContaining("APUESTAS_CORREO_REMITENTE");
    }
}

package es.ucm.fdi.is1.apuestas.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Publicada en Internet, la aplicación no arranca con la contraseña del creador sin definir. */
class ContrasenasPruebaTest {

    @Test
    void sinContrasenaDelCreadorNoArranca() {
        DatosIniciales datos = new DatosIniciales(null, null, null, null, null, null, "", "");

        assertThatThrownBy(() -> datos.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREADOR_PASSWORD");
    }
}

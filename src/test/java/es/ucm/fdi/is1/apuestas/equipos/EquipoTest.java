package es.ucm.fdi.is1.apuestas.equipos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** Criterio de aceptación de HU-01: la calificación debe estar entre 0 y 10. */
class EquipoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        factory.close();
    }

    @Test
    void aceptaCalificacionDentroDeRango() {
        assertThat(validator.validate(new Equipo("Real Madrid", "Fútbol", 9.5))).isEmpty();
    }

    @Test
    void rechazaCalificacionMayorQueDiez() {
        assertThat(validator.validate(new Equipo("Real Madrid", "Fútbol", 11.0))).isNotEmpty();
    }

    @Test
    void rechazaCalificacionNegativa() {
        assertThat(validator.validate(new Equipo("Lance Stroll", "Automovilismo", -1.0))).isNotEmpty();
    }
}

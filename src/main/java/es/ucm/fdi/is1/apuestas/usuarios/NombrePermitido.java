package es.ucm.fdi.is1.apuestas.usuarios;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Restricción de Bean Validation para el nombre visible: no puede contener palabras ofensivas
 * (issue #75). Se usa en {@link RegistroForm} (CA-11.3) y {@link NombreForm} (CA-47.3); la lista y la forma
 * de comparar están en {@link NombresProhibidos}.
 *
 * <p>Esto es una anotación propia ({@code @interface}), como {@code @NotBlank} pero hecha por nosotros: basta con
 * escribir {@code @NombrePermitido} encima de un campo para que {@code @Valid} lo compruebe. Las anotaciones de
 * encima dicen cómo se comporta: {@code @Constraint} indica qué clase la valida, {@code @Target(FIELD)} que se pone
 * en atributos y {@code @Retention(RUNTIME)} que sigue existiendo mientras el programa se ejecuta.
 * {@code message}, {@code groups} y {@code payload} los exige Bean Validation en toda restricción.
 */
@Documented
@Constraint(validatedBy = NombrePermitido.Validador.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NombrePermitido {

    String message() default "Ese nombre no está permitido. Elige otro sin palabras ofensivas";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Comprueba el nombre contra la lista. Un nombre vacío se da por bueno: eso ya lo controla {@code @NotBlank}. */
    class Validador implements ConstraintValidator<NombrePermitido, String> {

        @Override
        public boolean isValid(String nombre, ConstraintValidatorContext contexto) {
            return !NombresProhibidos.contiene(nombre);
        }
    }
}

package es.ucm.fdi.is1.apuestas.usuarios;

import java.nio.charset.StandardCharsets;

/**
 * BCrypt solo admite 72 bytes: con tildes o ñ, menos de 72 caracteres pueden pasar de ese límite.
 * Los formularios ya limitan a 72 caracteres con {@code @Size}; los controladores de registro,
 * cambio y recuperación de contraseña usan además esta comprobación en bytes.
 */
public final class Contrasenas {

    static final int MAXIMO_BYTES = 72;

    private Contrasenas() {
    }

    /** Si la contraseña cabe en BCrypt. Una contraseña null se da por buena: de eso se encarga @NotBlank. */
    public static boolean cabe(String password) {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= MAXIMO_BYTES;
    }

    public static final String DEMASIADO_LARGA =
            "La contraseña es demasiado larga: las letras con tilde y la ñ ocupan el doble. Usa una más corta.";
}

package es.ucm.fdi.is1.apuestas.usuarios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recuperar la contraseña (HU-17): se pide un enlace con el email y el enlace permite poner una nueva.
 * La respuesta es la misma exista o no la cuenta, para no desvelar qué emails están registrados.
 */
@Service
public class RecuperacionService {

    static final Duration VALIDEZ = Duration.ofMinutes(30);

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final TokenRecuperacionRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final EnvioCorreo correo;
    private final IntentosLogin intentos;
    private final Clock reloj;

    public RecuperacionService(UsuarioRepository usuarios, TokenRecuperacionRepository tokens,
                               PasswordEncoder passwordEncoder, EnvioCorreo correo, IntentosLogin intentos,
                               Clock reloj) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.correo = correo;
        this.intentos = intentos;
        this.reloj = reloj;
    }

    /**
     * Si hay una cuenta con ese email, crea un enlace nuevo (los anteriores dejan de servir) y lo envía.
     *
     * @param base dirección de la web, por ejemplo {@code https://apuestas-is1.onrender.com}
     */
    @Transactional
    public void solicitar(String email, String base) {
        String normalizado = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        Optional<Usuario> cuenta = usuarios.findByEmail(normalizado).filter(u -> !u.isEliminado());
        if (cuenta.isEmpty()) {
            return;
        }
        Usuario usuario = cuenta.get();
        tokens.borrarDe(usuario);
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new TokenRecuperacion(usuario, resumen(codigo), LocalDateTime.now(reloj).plus(VALIDEZ)));
        correo.enviarRecuperacion(usuario.getEmail(), usuario.getNombre(), base + "/recuperar/nueva?token=" + codigo);
    }

    /** Si el enlace sigue sirviendo (no ha caducado ni se ha usado). */
    @Transactional(readOnly = true)
    public boolean valido(String codigo) {
        return buscar(codigo).isPresent();
    }

    /**
     * Pone la contraseña nueva y gasta el enlace.
     *
     * @return el email de la cuenta, para cerrar sus sesiones abiertas
     * @throws EnlaceNoValidoException si el enlace ha caducado o ya se ha usado
     */
    @Transactional
    public String restablecer(String codigo, String nueva) {
        TokenRecuperacion token = buscar(codigo).orElseThrow(EnlaceNoValidoException::new);
        Usuario usuario = token.getUsuario();
        usuario.cambiarPasswordHash(passwordEncoder.encode(nueva));
        token.usar();
        intentos.olvidar(usuario.getEmail());
        return usuario.getEmail();
    }

    private Optional<TokenRecuperacion> buscar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return tokens.findByHash(resumen(codigo)).filter(t -> t.valido(ahora));
    }

    static String resumen(String codigo) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

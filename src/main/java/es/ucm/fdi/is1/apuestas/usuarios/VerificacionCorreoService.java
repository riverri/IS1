package es.ucm.fdi.is1.apuestas.usuarios;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates and consumes single-use links that verify mailbox ownership. */
@Service
public class VerificacionCorreoService {

    static final Duration VALIDEZ = Duration.ofHours(24);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final TokenVerificacionCorreoRepository tokens;
    private final EnvioCorreo correo;
    private final EnviosVerificacion envios;
    private final Clock reloj;

    public VerificacionCorreoService(UsuarioRepository usuarios, TokenVerificacionCorreoRepository tokens,
                                     EnvioCorreo correo, EnviosVerificacion envios, Clock reloj) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.correo = correo;
        this.envios = envios;
        this.reloj = reloj;
    }

    @Transactional
    public void solicitar(String email, String base) {
        String normalizado = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        Optional<Usuario> cuenta = usuarios.findByEmail(normalizado)
                .filter(usuario -> !usuario.isEliminado() && !usuario.isEmailVerificado());
        if (cuenta.isEmpty()) {
            return;
        }
        Usuario usuario = cuenta.get();
        if (!envios.permitir(usuario.getEmail())) {
            return;
        }
        tokens.borrarDe(usuario);
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new TokenVerificacionCorreo(usuario, RecuperacionService.resumen(codigo),
                LocalDateTime.now(reloj).plus(VALIDEZ)));
        correo.enviarVerificacion(usuario.getEmail(), usuario.getNombre(),
                base + "/verificar-correo/confirmar?token=" + codigo);
    }

    @Transactional(readOnly = true)
    public boolean valido(String codigo) {
        return buscar(codigo).isPresent();
    }

    @Transactional
    public void confirmar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new EnlaceVerificacionNoValidoException();
        }
        TokenVerificacionCorreo token = tokens.paraConfirmar(RecuperacionService.resumen(codigo))
                .filter(actual -> actual.valido(LocalDateTime.now(reloj)))
                .orElseThrow(EnlaceVerificacionNoValidoException::new);
        token.getUsuario().verificarEmail();
        token.usar();
        tokens.borrarDe(token.getUsuario());
    }

    private Optional<TokenVerificacionCorreo> buscar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return tokens.findByHash(RecuperacionService.resumen(codigo))
                .filter(token -> token.valido(LocalDateTime.now(reloj)));
    }
}

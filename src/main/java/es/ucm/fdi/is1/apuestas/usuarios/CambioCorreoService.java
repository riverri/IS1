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
import org.springframework.dao.DataIntegrityViolationException;

/** Reauthenticates email changes and applies them only after the new mailbox is confirmed. */
@Service
public class CambioCorreoService {

    static final Duration VALIDEZ = Duration.ofHours(24);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final CambioCorreoPendienteRepository cambios;
    private final UsuarioService usuarioService;
    private final EnvioCorreo correo;
    private final Clock reloj;

    public CambioCorreoService(UsuarioRepository usuarios, CambioCorreoPendienteRepository cambios,
                              UsuarioService usuarioService, EnvioCorreo correo, Clock reloj) {
        this.usuarios = usuarios;
        this.cambios = cambios;
        this.usuarioService = usuarioService;
        this.correo = correo;
        this.reloj = reloj;
    }

    @Transactional
    public void solicitar(String emailActual, String password, String emailNuevo, String base) {
        String actualNormalizado = normalizar(emailActual);
        if (!usuarioService.passwordCorrecta(actualNormalizado, password)) {
            throw new PasswordIncorrectaException();
        }
        Usuario usuario = usuarios.findByEmail(actualNormalizado).orElseThrow();
        String nuevoNormalizado = normalizar(emailNuevo);
        if (nuevoNormalizado.equals(actualNormalizado) || usuarios.existsByEmail(nuevoNormalizado)) {
            throw new EmailYaRegistradoException(nuevoNormalizado);
        }

        cambios.borrarDe(usuario);
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        cambios.save(new CambioCorreoPendiente(usuario, RecuperacionService.resumen(codigo), nuevoNormalizado,
                LocalDateTime.now(reloj).plus(VALIDEZ)));
        String enlace = base + "/cuenta/correo/confirmar?token=" + codigo;
        correo.enviarConfirmacionCambio(nuevoNormalizado, usuario.getNombre(), enlace);
        correo.notificarCambioSolicitado(actualNormalizado, usuario.getNombre(), nuevoNormalizado);
    }

    @Transactional(readOnly = true)
    public boolean valido(String codigo) {
        return buscar(codigo).isPresent();
    }

    @Transactional
    public String confirmar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new EnlaceVerificacionNoValidoException();
        }
        CambioCorreoPendiente cambio = cambios.paraConfirmar(RecuperacionService.resumen(codigo))
                .filter(actual -> actual.valido(LocalDateTime.now(reloj)))
                .orElseThrow(EnlaceVerificacionNoValidoException::new);
        Usuario usuario = cambio.getUsuario();
        if (usuarios.existsByEmail(cambio.getNuevoEmail())) {
            cambio.usar();
            throw new EmailYaRegistradoException(cambio.getNuevoEmail());
        }
        String emailAnterior = usuario.getEmail();
        usuario.cambiarEmailVerificado(cambio.getNuevoEmail());
        cambio.usar();
        cambios.borrarDe(usuario);
        try {
            usuarios.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EmailYaRegistradoException(cambio.getNuevoEmail());
        }
        return emailAnterior;
    }

    private Optional<CambioCorreoPendiente> buscar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return cambios.findByHash(RecuperacionService.resumen(codigo))
                .filter(cambio -> cambio.valido(LocalDateTime.now(reloj)));
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

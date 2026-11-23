package es.ucm.fdi.is1.apuestas.usuarios;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages mailbox confirmation before any user account or credential is created. */
@Service
public class RegistroCorreoService {

    static final Duration VALIDEZ = Duration.ofHours(24);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RegistroPendienteRepository registros;
    private final UsuarioRepository usuarios;
    private final EnvioCorreo correo;
    private final EnviosVerificacion envios;
    private final Clock reloj;

    public RegistroCorreoService(RegistroPendienteRepository registros, UsuarioRepository usuarios,
                                 EnvioCorreo correo, EnviosVerificacion envios, Clock reloj) {
        this.registros = registros;
        this.usuarios = usuarios;
        this.correo = correo;
        this.envios = envios;
        this.reloj = reloj;
    }

    @Transactional
    public void solicitar(String email, String base) {
        String normalizado = UsuarioService.normalizar(email);
        if (usuarios.existsByEmail(normalizado) || normalizado.endsWith(Usuario.DOMINIO_ELIMINADOS)
                || !envios.permitir(normalizado)) {
            return;
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = RecuperacionService.resumen(token);
        LocalDateTime caducidad = LocalDateTime.now(reloj).plus(VALIDEZ);
        RegistroPendiente registro = registros.findByEmail(normalizado)
                .map(pendiente -> {
                    pendiente.renovar(tokenHash, caducidad);
                    return pendiente;
                })
                .orElseGet(() -> new RegistroPendiente(normalizado, tokenHash, caducidad));
        registros.saveAndFlush(registro);
        correo.enviarVerificacion(normalizado, "", base + "/registro/confirmar?token=" + token);
    }

    @Transactional(readOnly = true)
    public boolean valido(String token) {
        return buscar(token).isPresent();
    }

    @Transactional(readOnly = true)
    public Optional<String> emailConfirmadoPor(String token) {
        return buscar(token).map(RegistroPendiente::getEmail);
    }

    @Transactional
    public String confirmar(String token) {
        if (token == null || token.isBlank()) {
            throw new EnlaceVerificacionNoValidoException();
        }
        RegistroPendiente registro = registros.paraConfirmar(RecuperacionService.resumen(token))
                .filter(actual -> actual.valido(LocalDateTime.now(reloj)))
                .orElseThrow(EnlaceVerificacionNoValidoException::new);
        registros.delete(registro);
        return registro.getEmail();
    }

    @Scheduled(initialDelayString = "PT5M", fixedDelayString = "PT1H")
    @Transactional
    public int borrarRegistrosCaducados() {
        return registros.borrarCaducados(LocalDateTime.now(reloj));
    }

    private Optional<RegistroPendiente> buscar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return registros.findByTokenHash(RecuperacionService.resumen(token))
                .filter(registro -> registro.valido(LocalDateTime.now(reloj)));
    }
}

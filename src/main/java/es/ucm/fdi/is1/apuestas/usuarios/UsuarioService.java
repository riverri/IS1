package es.ucm.fdi.is1.apuestas.usuarios;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final SaldoProperties saldo;
    private final Clock reloj;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
                          SaldoProperties saldo, Clock reloj) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.saldo = saldo;
        this.reloj = reloj;
    }

    /** Crea la cuenta con el saldo de bienvenida (HU-11, HU-14). */
    @Transactional
    public Usuario registrar(RegistroForm form) {
        return crear(form.getEmail(), form.getNombre(), form.getPassword(), Rol.USUARIO);
    }

    @Transactional
    public Usuario crear(String email, String nombre, String password, Rol rol) {
        String emailNormalizado = normalizar(email);
        if (usuarios.existsByEmail(emailNormalizado)) {
            throw new EmailYaRegistradoException(emailNormalizado);
        }
        Usuario usuario = new Usuario(emailNormalizado, nombre.trim(), passwordEncoder.encode(password), rol,
                saldo.bienvenida(), LocalDateTime.now(reloj));
        return usuarios.save(usuario);
    }

    /** Devuelve el usuario aplicando antes la recarga gratuita si le corresponde (HU-13, HU-14). */
    @Transactional
    public Usuario consultarConRecarga(String email) {
        Usuario usuario = usuarios.findByEmail(normalizar(email)).orElseThrow();
        usuario.aplicarRecargaPeriodica(LocalDateTime.now(reloj), saldo.recargaPeriodo(), saldo.recargaImporte());
        return usuario;
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscar(String email) {
        return usuarios.findByEmail(normalizar(email));
    }

    static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

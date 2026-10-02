package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    static final Set<Integer> DIAS_DE_PAUSA = Set.of(1, 7, 30);

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

    /** Cambia el nombre visible (HU-47). */
    @Transactional
    public void cambiarNombre(String email, String nombre) {
        usuarios.findByEmail(normalizar(email)).orElseThrow().cambiarNombre(nombre.trim());
    }

    /** Cambia la contraseña si la actual es correcta (HU-47). */
    @Transactional
    public void cambiarPassword(String email, String actual, String nueva) {
        Usuario usuario = usuarios.findByEmail(normalizar(email)).orElseThrow();
        if (!passwordEncoder.matches(actual, usuario.getPasswordHash())) {
            throw new PasswordIncorrectaException();
        }
        usuario.cambiarPasswordHash(passwordEncoder.encode(nueva));
    }

    /** Límites diario y semanal que se pone el usuario; null quita el límite (HU-10). */
    @Transactional
    public void fijarLimites(String email, BigDecimal diario, BigDecimal semanal) {
        if (diario != null && semanal != null && diario.compareTo(semanal) > 0) {
            throw new IllegalArgumentException("El límite diario no puede ser mayor que el semanal");
        }
        usuarios.findByEmail(normalizar(email)).orElseThrow().fijarLimites(diario, semanal);
    }

    /** Pausa las apuestas durante unos días; no se puede deshacer antes de tiempo (HU-10). */
    @Transactional
    public LocalDateTime pausar(String email, int dias) {
        if (!DIAS_DE_PAUSA.contains(dias)) {
            throw new IllegalArgumentException("Elige una pausa de 1, 7 o 30 días");
        }
        Usuario usuario = usuarios.findByEmail(normalizar(email)).orElseThrow();
        usuario.pausarHasta(LocalDateTime.now(reloj).plusDays(dias));
        return usuario.getPausaHasta();
    }

    /** Comprueba la contraseña del usuario, por ejemplo antes de eliminar la cuenta (HU-18). */
    @Transactional(readOnly = true)
    public boolean passwordCorrecta(String email, String password) {
        return password != null && usuarios.findByEmail(normalizar(email))
                .map(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElse(false);
    }

    /** Anonimiza la cuenta: los datos personales desaparecen y ya no se puede entrar con ella (HU-18). */
    @Transactional
    public void anonimizar(Usuario usuario) {
        usuario.eliminar(passwordEncoder.encode(UUID.randomUUID().toString()));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscar(String email) {
        return usuarios.findByEmail(normalizar(email));
    }

    static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

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

/**
 * Servicio de las cuentas: registro, saldo con recargas gratuitas, nombre y contraseña, juego responsable
 * y anonimización al eliminar la cuenta (HU-10, HU-11, HU-13, HU-14, HU-18, HU-47).
 * Las contraseñas se cifran con el {@link PasswordEncoder} de {@code SeguridadConfig}, que es BCrypt.
 * Los emails se buscan siempre normalizados, así que da igual cómo los escriba el usuario.
 * Los métodos que cambian un usuario no llaman a {@code save}: el usuario lo ha cargado JPA dentro
 * de la transacción y sus cambios se guardan solos al terminarla.
 *
 * <p>{@code @Service}: Spring crea un único objeto de esta clase y se lo pasa a quien lo necesite (por ejemplo,
 * a {@link UsuariosController}). {@code @Transactional} en un método: todo lo que hace en la base de datos se
 * confirma junto al terminar, o se deshace entero si lanza una excepción. Con {@code readOnly = true} se avisa
 * de que solo lee, y la base de datos puede ir más rápido.
 */
@Service
public class UsuarioService {

    /** Duraciones de pausa (en días) que se pueden elegir en Mi cuenta (HU-10). */
    static final Set<Integer> DIAS_DE_PAUSA = Set.of(1, 7, 30);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final SaldoProperties saldo;
    private final Clock reloj;

    /** Inyección de dependencias: Spring llama a este constructor y le pasa los objetos que pide. */
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

    /**
     * Crea una cuenta con el rol indicado y el saldo de bienvenida. También la usan los datos iniciales
     * para el creador de apuestas y el usuario de prueba.
     *
     * @throws EmailYaRegistradoException si ya hay una cuenta con ese email o usa el dominio reservado
     */
    @Transactional
    public Usuario crear(String email, String nombre, String password, Rol rol) {
        String emailNormalizado = normalizar(email);
        // El dominio de las cuentas eliminadas está reservado: si alguien lo ocupara, impediría eliminar esa cuenta
        if (usuarios.existsByEmail(emailNormalizado) || emailNormalizado.endsWith(Usuario.DOMINIO_ELIMINADOS)) {
            throw new EmailYaRegistradoException(emailNormalizado);
        }
        // encode aplica BCrypt con una sal aleatoria: se guarda el resumen, nunca la contraseña
        Usuario usuario = new Usuario(emailNormalizado, nombre.trim(), passwordEncoder.encode(password), rol,
                saldo.bienvenida(), LocalDateTime.now(reloj));
        return usuarios.save(usuario);
    }

    /**
     * Devuelve el usuario aplicando antes la recarga gratuita si le corresponde (HU-13, HU-14).
     * Se llama al iniciar sesión y al abrir Mi cuenta.
     */
    @Transactional
    public Usuario consultarConRecarga(String email) {
        // findByEmail devuelve un Optional; orElseThrow saca el usuario o lanza una excepción si no existe
        Usuario usuario = usuarios.findByEmail(normalizar(email)).orElseThrow();
        usuario.aplicarRecargaPeriodica(LocalDateTime.now(reloj), saldo.recargaPeriodo(), saldo.recargaImporte());
        return usuario;
    }

    /** Cambia el nombre visible (HU-47). */
    @Transactional
    public void cambiarNombre(String email, String nombre) {
        usuarios.findByEmail(normalizar(email)).orElseThrow().cambiarNombre(nombre.trim());
    }

    /**
     * Cambia la contraseña si la actual es correcta (HU-47).
     *
     * @throws PasswordIncorrectaException si la contraseña actual no coincide (CA-47.2)
     */
    @Transactional
    public void cambiarPassword(String email, String actual, String nueva) {
        Usuario usuario = usuarios.findByEmail(normalizar(email)).orElseThrow();
        // BCrypt no se puede descifrar: matches cifra la contraseña escrita con la sal del resumen y los compara
        if (!passwordEncoder.matches(actual, usuario.getPasswordHash())) {
            throw new PasswordIncorrectaException();
        }
        usuario.cambiarPasswordHash(passwordEncoder.encode(nueva));
    }

    /** Pone esta contraseña si no es ya la actual (contraseña del creador en los perfiles públicos). */
    @Transactional
    public void fijarPassword(String email, String password) {
        // Se comprueba antes: con su sal aleatoria, BCrypt daría un resumen nuevo en cada arranque.
        // filter deja el Optional vacío si la contraseña ya es esa, y entonces ifPresent no hace nada
        usuarios.findByEmail(normalizar(email))
                .filter(u -> !passwordEncoder.matches(password, u.getPasswordHash()))
                .ifPresent(u -> u.cambiarPasswordHash(passwordEncoder.encode(password)));
    }

    /**
     * Límites diario y semanal que se pone el usuario; null quita el límite (HU-10).
     *
     * @throws IllegalArgumentException si el límite diario es mayor que el semanal
     */
    @Transactional
    public void fijarLimites(String email, BigDecimal diario, BigDecimal semanal) {
        if (diario != null && semanal != null && diario.compareTo(semanal) > 0) {
            throw new IllegalArgumentException("El límite diario no puede ser mayor que el semanal");
        }
        usuarios.findByEmail(normalizar(email)).orElseThrow().fijarLimites(diario, semanal);
    }

    /**
     * Pausa las apuestas durante unos días; no se puede deshacer antes de tiempo (HU-10).
     *
     * @return cuándo termina la pausa (si ya había una más larga, la de esa)
     * @throws IllegalArgumentException si los días no son 1, 7 o 30
     */
    @Transactional
    public LocalDateTime pausar(String email, int dias) {
        if (!DIAS_DE_PAUSA.contains(dias)) {
            throw new IllegalArgumentException("Elige una pausa de 1, 7 o 30 días");
        }
        Usuario usuario = usuarios.findByEmail(normalizar(email)).orElseThrow();
        usuario.pausarHasta(LocalDateTime.now(reloj).plusDays(dias));
        return usuario.getPausaHasta();
    }

    /**
     * Comprueba la contraseña del usuario, por ejemplo antes de eliminar la cuenta (HU-18).
     * Devuelve false si la contraseña es null o no existe la cuenta.
     */
    @Transactional(readOnly = true)
    public boolean passwordCorrecta(String email, String password) {
        // map transforma el usuario (si existe) en true/false; si no existe, orElse(false)
        return password != null && usuarios.findByEmail(normalizar(email))
                .map(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElse(false);
    }

    /** Anonimiza la cuenta: los datos personales desaparecen y ya no se puede entrar con ella (HU-18). */
    @Transactional
    public void anonimizar(Usuario usuario) {
        // La contraseña pasa a ser un UUID aleatorio que no se guarda en ningún sitio: nadie la conoce
        usuario.eliminar(passwordEncoder.encode(UUID.randomUUID().toString()));
    }

    /** Busca la cuenta por email, sin aplicar la recarga. */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscar(String email) {
        return usuarios.findByEmail(normalizar(email));
    }

    /** Sin espacios alrededor y en minúsculas: " Ana@Mail.es" y "ana@mail.es" son la misma cuenta. */
    static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/**
 * Entidad JPA: cuenta de usuario con su saldo de moneditas virtuales (HU-11, HU-13, HU-14).
 * Sirve tanto para los jugadores como para el creador de apuestas (ver {@link Rol}).
 * Aquí están las reglas del saldo (cargar, abonar, recargas gratuitas), los límites del juego
 * responsable (HU-10), el cambio de nombre y contraseña (HU-47) y la baja (HU-18).
 * {@link UsuarioService} llama a estos métodos dentro de una transacción.
 *
 * <p><b>Qué es una entidad:</b> {@code @Entity} indica a JPA (la librería que conecta Java con la base de datos)
 * que cada objeto {@code Usuario} es una fila de la tabla {@code usuario}, y cada atributo, una columna.
 * Así no hace falta escribir SQL para guardarlo o leerlo: lo hace el {@link UsuarioRepository}.
 * Los atributos son {@code private}: solo se cambian a través de los métodos de esta clase, que son los que
 * conocen las reglas (por ejemplo, que el saldo no puede quedar negativo al apostar).
 */
@Entity
public class Usuario {

    /** Dominio de los emails de las cuentas eliminadas; nadie puede registrarse con él. */
    public static final String DOMINIO_ELIMINADOS = "@apuestas.invalid";

    /**
     * Control de concurrencia: si dos operaciones cambian el saldo a la vez (doble clic, dos pestañas,
     * una apuesta mientras se paga otra), la segunda falla en lugar de pisar a la primera.
     * JPA suma 1 a este número cada vez que guarda el usuario; si al guardar ve que otro ya lo había cambiado,
     * lanza una excepción (la recoge {@code web.ConcurrenciaAdvice}).
     */
    @Version
    private long version;

    // @Id: clave primaria (identifica la fila). @GeneratedValue: la base de datos le asigna el número al guardar
    @Id
    @GeneratedValue
    private Long id;

    /** Se guarda normalizado (sin espacios y en minúsculas) y es único. */
    // @Column configura la columna: nullable = false → obligatoria; unique = true → no puede repetirse
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nombre;

    /** Resumen BCrypt de la contraseña. La contraseña en claro no se guarda nunca. */
    @Column(nullable = false)
    private String passwordHash;

    // EnumType.STRING: se guarda el texto "USUARIO" o "CREADOR" (y no su posición 0/1, que cambiaría si se reordenan)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Rol rol;

    /**
     * Moneditas disponibles. BigDecimal con 2 decimales: sin los errores de redondeo de double.
     * (Con double, 0.1 + 0.2 da 0.30000000000000004; con dinero eso no se puede permitir. BigDecimal no se suma con
     * {@code +} sino con {@code add}, y no se compara con {@code <} sino con {@code compareTo}.)
     * precision = 12, scale = 2: hasta 12 cifras en total, 2 de ellas decimales.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo;

    /** Última recarga gratuita (la de bienvenida cuenta como la primera). */
    @Column(nullable = false)
    private LocalDateTime ultimaRecarga;

    /** Juego responsable (HU-10): máximo apostado en 24 horas; null = sin límite. */
    @Column(precision = 12, scale = 2)
    private BigDecimal limiteDiario;

    /** Juego responsable (HU-10): máximo apostado en 7 días; null = sin límite. */
    @Column(precision = 12, scale = 2)
    private BigDecimal limiteSemanal;

    /** Juego responsable (HU-10): no puede apostar hasta esta fecha. */
    private LocalDateTime pausaHasta;

    /** Cuenta eliminada y anonimizada (HU-18). */
    @Column(nullable = false)
    private boolean eliminado;

    protected Usuario() {
        // requerido por JPA: al leer de la base de datos crea el objeto vacío y luego rellena los atributos.
        // Es protected para que el resto del código use el constructor de abajo, que deja el usuario completo
    }

    /**
     * Cuenta nueva con el saldo de bienvenida (HU-14). La fecha de alta cuenta como la primera recarga.
     *
     * @param passwordHash la contraseña ya cifrada con BCrypt
     */
    public Usuario(String email, String nombre, String passwordHash, Rol rol,
                   BigDecimal saldoBienvenida, LocalDateTime fechaAlta) {
        this.email = email;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.saldo = saldoBienvenida;
        this.ultimaRecarga = fechaAlta;
    }

    /**
     * Si ha pasado el periodo desde la última recarga gratuita, suma el importe al saldo (HU-14, CA-14.2).
     *
     * @return true si se ha aplicado la recarga
     */
    public boolean aplicarRecargaPeriodica(LocalDateTime ahora, Duration periodo, BigDecimal importe) {
        if (ahora.isBefore(proximaRecarga(periodo))) {
            return false;
        }
        saldo = saldo.add(importe);
        // El periodo vuelve a contar desde hoy: si pasa varias semanas sin entrar, recibe una sola recarga
        ultimaRecarga = ahora;
        return true;
    }

    /**
     * Descuenta el importe de una apuesta (HU-23). El saldo nunca queda negativo por apostar.
     *
     * @throws SaldoInsuficienteException si el saldo no llega al importe
     */
    public void cargar(BigDecimal importe) {
        // compareTo devuelve un número negativo si saldo < importe, 0 si son iguales y positivo si saldo > importe
        if (saldo.compareTo(importe) < 0) {
            throw new SaldoInsuficienteException(saldo, importe);
        }
        saldo = saldo.subtract(importe);
    }

    /**
     * Ajuste por corrección de un resultado: puede restar una ganancia ya pagada.
     * A diferencia de {@link #cargar}, permite que el saldo quede negativo.
     * Una cuenta eliminada no recibe dinero: sus apuestas se resuelven igual, pero sin abonarle nada.
     */
    public void ajustar(BigDecimal diferencia) {
        if (eliminado) {
            return;
        }
        saldo = saldo.add(diferencia);
    }

    /** Devuelve o abona un importe al saldo (cancelaciones y ganancias). Si la cuenta está eliminada, nada. */
    public void abonar(BigDecimal importe) {
        if (eliminado) {
            return;
        }
        saldo = saldo.add(importe);
    }

    /** Cambia el nombre visible (HU-47). */
    public void cambiarNombre(String nuevo) {
        nombre = nuevo;
    }

    /** HU-47: recibe la contraseña ya cifrada. */
    public void cambiarPasswordHash(String nuevoHash) {
        passwordHash = nuevoHash;
    }

    /** Fecha a partir de la cual toca la siguiente recarga gratuita (HU-14). */
    public LocalDateTime proximaRecarga(Duration periodo) {
        return ultimaRecarga.plus(periodo);
    }

    /** HU-10: null quita el límite. No se comprueba aquí que el diario no supere al semanal. */
    public void fijarLimites(BigDecimal diario, BigDecimal semanal) {
        limiteDiario = diario;
        limiteSemanal = semanal;
    }

    /**
     * HU-10: pausa las apuestas hasta la fecha indicada. Una pausa no se puede acortar:
     * si ya había una más larga, se mantiene.
     */
    public void pausarHasta(LocalDateTime hasta) {
        if (pausaHasta == null || hasta.isAfter(pausaHasta)) {
            pausaHasta = hasta;
        }
    }

    /** HU-10: si tiene una pausa que aún no ha terminado. */
    public boolean enPausa(LocalDateTime ahora) {
        return pausaHasta != null && pausaHasta.isAfter(ahora);
    }

    /**
     * HU-18: borra los datos personales y deja la cuenta inutilizable. Se conserva la fila para que
     * las apuestas ya hechas y su historial sigan cuadrando.
     *
     * @param hashInutilizable resumen BCrypt de una contraseña aleatoria que nadie conoce
     */
    public void eliminar(String hashInutilizable) {
        // El id hace que el email siga siendo único (la columna lo exige)
        email = "eliminado-" + id + DOMINIO_ELIMINADOS;
        nombre = "Usuario eliminado";
        passwordHash = hashInutilizable;
        saldo = BigDecimal.ZERO;
        limiteDiario = null;
        limiteSemanal = null;
        pausaHasta = null;
        eliminado = true;
    }

    // Getters: dan acceso de solo lectura a los atributos. Las plantillas HTML (Thymeleaf) los usan también:
    // ${usuario.saldo} llama a getSaldo(), y ${usuario.creador} a isCreador() (los boolean empiezan por "is")

    public BigDecimal getLimiteDiario() {
        return limiteDiario;
    }

    public BigDecimal getLimiteSemanal() {
        return limiteSemanal;
    }

    public LocalDateTime getPausaHasta() {
        return pausaHasta;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public boolean isCreador() {
        return rol == Rol.CREADOR;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public LocalDateTime getUltimaRecarga() {
        return ultimaRecarga;
    }
}

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

/** Cuenta de usuario con su saldo de moneditas virtuales (HU-11, HU-13, HU-14). */
@Entity
public class Usuario {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo;

    /** Última recarga gratuita (la de bienvenida cuenta como la primera). */
    @Column(nullable = false)
    private LocalDateTime ultimaRecarga;

    protected Usuario() {
        // requerido por JPA
    }

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
     * Si ha pasado el periodo desde la última recarga gratuita, suma el importe al saldo.
     *
     * @return true si se ha aplicado la recarga
     */
    public boolean aplicarRecargaPeriodica(LocalDateTime ahora, Duration periodo, BigDecimal importe) {
        if (ahora.isBefore(proximaRecarga(periodo))) {
            return false;
        }
        saldo = saldo.add(importe);
        ultimaRecarga = ahora;
        return true;
    }

    public LocalDateTime proximaRecarga(Duration periodo) {
        return ultimaRecarga.plus(periodo);
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

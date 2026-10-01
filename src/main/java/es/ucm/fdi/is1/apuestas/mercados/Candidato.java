package es.ucm.fdi.is1.apuestas.mercados;

import java.math.BigDecimal;

import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Uno de los posibles ganadores de un mercado, con la cuota que fija el creador (HU-45). */
@Entity
public class Candidato {

    static final BigDecimal CUOTA_MINIMA = new BigDecimal("1.01");
    static final BigDecimal CUOTA_MAXIMA = new BigDecimal("1000.00");

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Mercado mercado;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal cuota;

    /** Equipo o deportista del catálogo, si lo es, para mostrar su escudo. Opcional. */
    @ManyToOne
    private Equipo equipo;

    protected Candidato() {
        // requerido por JPA
    }

    Candidato(Mercado mercado, String nombre, BigDecimal cuota) {
        this.mercado = mercado;
        this.nombre = nombre;
        cambiarCuota(cuota);
    }

    /** Las apuestas ya hechas conservan la cuota con la que se hicieron. */
    public void cambiarCuota(BigDecimal nueva) {
        if (nueva == null || nueva.compareTo(CUOTA_MINIMA) < 0 || nueva.compareTo(CUOTA_MAXIMA) > 0) {
            throw new IllegalArgumentException("La cuota debe estar entre 1,01 y 1000");
        }
        if (nueva.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("La cuota admite como mucho 2 decimales");
        }
        cuota = nueva.setScale(2);
    }

    public Long getId() {
        return id;
    }

    public Mercado getMercado() {
        return mercado;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getCuota() {
        return cuota;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }
}

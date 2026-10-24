package es.ucm.fdi.is1.apuestas.cripto;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Configuración de la sección de criptomonedas que fija el creador (HU-73). Una sola fila, creada por la migración V15,
 * como {@code apuesta.Limites} y {@code casino.ConfiguracionCasino}. Los importes mínimo y máximo de cada compra son
 * los límites de apuesta de siempre (HU-07).
 */
@Entity
public class ConfiguracionCripto {

    static final Long ID = 1L;

    @Id
    private Long id;

    @Column(nullable = false)
    private boolean activo;

    /** Parte de cada compra y venta que se queda la casa (0,01 = 1 %). */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal comision;

    /** Minutos que sirve un precio descargado; con uno más viejo no se compra ni se vende. */
    @Column(nullable = false)
    private int minutosValidez;

    protected ConfiguracionCripto() {
        // requerido por JPA
    }

    /**
     * @throws IllegalArgumentException si la comisión no está entre 0 y 10 % o la validez no está entre 1 y 120 minutos
     */
    void cambiar(boolean nuevoActivo, BigDecimal nuevaComision, int minutos) {
        if (nuevaComision.signum() < 0 || nuevaComision.compareTo(new BigDecimal("0.10")) > 0) {
            throw new IllegalArgumentException("La comisión va del 0 al 10 %");
        }
        if (minutos < 1 || minutos > 120) {
            throw new IllegalArgumentException("Un precio puede servir entre 1 y 120 minutos");
        }
        activo = nuevoActivo;
        comision = nuevaComision;
        minutosValidez = minutos;
    }

    public boolean isActivo() {
        return activo;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public int getMinutosValidez() {
        return minutosValidez;
    }
}

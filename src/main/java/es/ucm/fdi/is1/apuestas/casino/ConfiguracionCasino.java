package es.ucm.fdi.is1.apuestas.casino;

import java.math.BigDecimal;
import java.math.RoundingMode;

import es.ucm.fdi.is1.apuestas.apuesta.ImporteFueraDeLimitesException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

/**
 * Configuración del casino que fija el creador (HU-59). Hay una sola fila, creada por la migración V13, igual que
 * {@code apuesta.Limites}: no se crean ni se borran filas desde el código.
 *
 * <p>La ventaja de la casa se controla con reglas reales: la ruleta europea siempre tiene un 2,70 % (un 0 entre 37
 * números), y en el blackjack la cambian el número de barajas, si la banca pide con 17 blando y si el blackjack
 * paga 3:2 o 6:5.
 */
@Entity
public class ConfiguracionCasino {

    /** Id de la única fila. */
    static final Long ID = 1L;

    @Id
    private Long id;

    /** Si es false, nadie puede jugar (CA-59.2). */
    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importeMinimo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importeMaximo;

    /** Barajas del blackjack, de 1 a 8. */
    @Column(nullable = false)
    private int barajas;

    @Column(nullable = false)
    private boolean bancaPideCon17Blando;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PagoBlackjack pagoBlackjack;

    protected ConfiguracionCasino() {
        // requerido por JPA
    }

    /**
     * Cambia la configuración (CA-59.1); vale para las partidas nuevas.
     *
     * @throws IllegalArgumentException si el mínimo no es positivo, el máximo es menor que el mínimo o las barajas no
     *                                  están entre 1 y 8
     */
    public void cambiar(boolean nuevoActivo, BigDecimal minimo, BigDecimal maximo, int nuevasBarajas,
                        boolean pideCon17Blando, PagoBlackjack pago) {
        if (minimo.signum() <= 0) {
            throw new IllegalArgumentException("El importe mínimo debe ser mayor que 0");
        }
        if (maximo.compareTo(minimo) < 0) {
            throw new IllegalArgumentException("El importe máximo no puede ser menor que el mínimo");
        }
        if (nuevasBarajas < 1 || nuevasBarajas > 8) {
            throw new IllegalArgumentException("El blackjack se juega con entre 1 y 8 barajas");
        }
        activo = nuevoActivo;
        importeMinimo = minimo;
        importeMaximo = maximo;
        barajas = nuevasBarajas;
        bancaPideCon17Blando = pideCon17Blando;
        pagoBlackjack = pago;
    }

    /** Mismo mensaje que los límites de las apuestas deportivas (CA-56.3). */
    void comprobarImporte(BigDecimal importe) {
        if (importe.compareTo(importeMinimo) < 0 || importe.compareTo(importeMaximo) > 0) {
            throw new ImporteFueraDeLimitesException(importeMinimo, importeMaximo);
        }
    }

    /** Ventaja de la casa en la ruleta europea: 1/37, en % con dos decimales (2,70 %). */
    public BigDecimal getVentajaRuleta() {
        return BigDecimal.valueOf(100).divide(BigDecimal.valueOf(37), 2, RoundingMode.HALF_UP);
    }

    /**
     * Ventaja aproximada de la casa en el blackjack, en %, jugando con la estrategia básica. Son valores orientativos
     * de las tablas de reglas habituales: más barajas, banca que pide con 17 blando, pago 6:5 y no poder dividir
     * suben la ventaja de la casa.
     */
    public BigDecimal getVentajaBlackjack() {
        double base = barajas == 1 ? 0.0 : barajas == 2 ? 0.3 : barajas <= 4 ? 0.5 : barajas <= 6 ? 0.55 : 0.6;
        double ventaja = base
                + (bancaPideCon17Blando ? 0.2 : 0)
                + (pagoBlackjack == PagoBlackjack.SEIS_A_CINCO ? 1.4 : 0)
                // Aquí no se puede dividir una pareja (fuera del alcance de HU-57)
                + 0.5;
        return BigDecimal.valueOf(ventaja).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isActivo() {
        return activo;
    }

    public BigDecimal getImporteMinimo() {
        return importeMinimo;
    }

    public BigDecimal getImporteMaximo() {
        return importeMaximo;
    }

    public int getBarajas() {
        return barajas;
    }

    public boolean isBancaPideCon17Blando() {
        return bancaPideCon17Blando;
    }

    public PagoBlackjack getPagoBlackjack() {
        return pagoBlackjack;
    }
}

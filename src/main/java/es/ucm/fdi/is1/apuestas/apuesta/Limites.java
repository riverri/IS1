package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Límites de apuesta que fija el creador (HU-07). Hay una sola fila, creada por la migración V3.
 */
@Entity
public class Limites {

    static final Long ID = 1L;

    @Id
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importeMinimo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importeMaximo;

    /** Número máximo de selecciones en una combinada. */
    @Column(nullable = false)
    private int maxSelecciones;

    /**
     * Si es false, no se aplican los límites personales ni las pausas de los usuarios (HU-10) y la opción
     * desaparece de Mi cuenta. Lo que cada usuario tenga guardado se conserva para cuando se vuelva a activar.
     */
    @Column(nullable = false)
    private boolean juegoResponsable = true;

    protected Limites() {
        // requerido por JPA
    }

    public void cambiar(BigDecimal minimo, BigDecimal maximo, int selecciones) {
        if (minimo.signum() <= 0) {
            throw new IllegalArgumentException("El importe mínimo debe ser mayor que 0");
        }
        if (maximo.compareTo(minimo) < 0) {
            throw new IllegalArgumentException("El importe máximo no puede ser menor que el mínimo");
        }
        if (selecciones < 2) {
            throw new IllegalArgumentException("Una combinada necesita al menos 2 selecciones");
        }
        importeMinimo = minimo;
        importeMaximo = maximo;
        maxSelecciones = selecciones;
    }

    /** Lanza una excepción si el importe no está entre el mínimo y el máximo. */
    public void comprobarImporte(BigDecimal importe) {
        if (importe.compareTo(importeMinimo) < 0 || importe.compareTo(importeMaximo) > 0) {
            throw new ImporteFueraDeLimitesException(importeMinimo, importeMaximo);
        }
    }

    public void comprobarSelecciones(int selecciones) {
        if (selecciones > maxSelecciones) {
            throw new IllegalArgumentException("Una combinada admite como máximo " + maxSelecciones + " selecciones");
        }
    }

    public void activarJuegoResponsable(boolean activo) {
        juegoResponsable = activo;
    }

    public boolean isJuegoResponsable() {
        return juegoResponsable;
    }

    public BigDecimal getImporteMinimo() {
        return importeMinimo;
    }

    public BigDecimal getImporteMaximo() {
        return importeMaximo;
    }

    public int getMaxSelecciones() {
        return maxSelecciones;
    }
}

package es.ucm.fdi.is1.apuestas.cripto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Entidad JPA: un precio descargado, para dibujar cómo se ha movido (HU-69). Se guarda uno en cada descarga y se
 * borran los de más de 7 días.
 */
@Entity
public class PrecioCripto {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Criptomoneda cripto;

    @Column(nullable = false, precision = 30, scale = 12)
    private BigDecimal precio;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected PrecioCripto() {
        // requerido por JPA
    }

    PrecioCripto(Criptomoneda cripto, BigDecimal precio, LocalDateTime fecha) {
        this.cripto = cripto;
        this.precio = precio;
        this.fecha = fecha;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

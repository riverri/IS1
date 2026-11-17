package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/**
 * Entidad JPA: cuota mejorada por el creador para un resultado de un evento durante un tiempo (HU-87). Solo vale en
 * apuestas simples y hasta un importe máximo. La aplica {@link ApuestaService}; la gestiona {@link SupercuotaService}.
 */
@Entity
public class Supercuota {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Evento evento;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Resultado resultado;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal cuota;

    /** Lo máximo que se puede apostar con esta cuota en una apuesta. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importeMaximo;

    /** Hasta cuándo vale; nunca después del inicio del partido. */
    @Column(nullable = false)
    private LocalDateTime hasta;

    @Column(nullable = false)
    private LocalDateTime creada;

    protected Supercuota() {
        // requerido por JPA
    }

    Supercuota(Evento evento, Resultado resultado, BigDecimal cuota, BigDecimal importeMaximo, LocalDateTime hasta,
               LocalDateTime creada) {
        this.evento = evento;
        this.resultado = resultado;
        this.cuota = cuota;
        this.importeMaximo = importeMaximo;
        this.hasta = hasta;
        this.creada = creada;
    }

    public boolean isVigente(LocalDateTime ahora) {
        return ahora.isBefore(hasta) && evento.admiteApuestas(ahora);
    }

    void terminar(LocalDateTime ahora) {
        hasta = ahora;
    }

    public Long getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public Resultado getResultado() {
        return resultado;
    }

    public BigDecimal getCuota() {
        return cuota;
    }

    public BigDecimal getImporteMaximo() {
        return importeMaximo;
    }

    public LocalDateTime getHasta() {
        return hasta;
    }
}

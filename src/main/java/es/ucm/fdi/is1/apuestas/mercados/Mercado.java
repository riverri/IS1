package es.ucm.fdi.is1.apuestas.mercados;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

/**
 * Apuesta a largo plazo con varios candidatos: Balón de Oro, campeón de liga… (HU-44, HU-45).
 * El creador fija la cuota de cada candidato, lo cierra y marca el ganador.
 */
@Entity
public class Mercado {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Deporte deporte;

    /** Último momento para apostar. */
    @Column(nullable = false)
    private LocalDateTime cierre;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoMercado estado = EstadoMercado.ABIERTO;

    @OneToMany(mappedBy = "mercado", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("cuota, nombre")
    private List<Candidato> candidatos = new ArrayList<>();

    @ManyToOne
    private Candidato ganador;

    protected Mercado() {
        // requerido por JPA
    }

    public Mercado(String nombre, Deporte deporte, LocalDateTime cierre) {
        this.nombre = nombre;
        this.deporte = deporte;
        this.cierre = cierre;
    }

    public Candidato anadirCandidato(String nombreCandidato, BigDecimal cuota) {
        if (estado != EstadoMercado.ABIERTO) {
            throw new IllegalStateException("Solo se pueden añadir candidatos a un mercado abierto");
        }
        if (nombreCandidato == null || nombreCandidato.isBlank() || nombreCandidato.length() > 100) {
            throw new IllegalArgumentException("El nombre de cada candidato debe tener entre 1 y 100 caracteres");
        }
        boolean repetido = candidatos.stream().anyMatch(c -> c.getNombre().equalsIgnoreCase(nombreCandidato));
        if (repetido) {
            throw new IllegalArgumentException(nombreCandidato + " ya es candidato");
        }
        Candidato candidato = new Candidato(this, nombreCandidato, cuota);
        candidatos.add(candidato);
        return candidato;
    }

    public boolean admiteApuestas(LocalDateTime ahora) {
        return estado == EstadoMercado.ABIERTO && cierre.isAfter(ahora);
    }

    /** Deja de admitir apuestas antes de la fecha de cierre. */
    public void cerrar() {
        if (estado != EstadoMercado.ABIERTO) {
            throw new IllegalStateException("Solo se puede cerrar un mercado abierto");
        }
        estado = EstadoMercado.CERRADO;
    }

    /** Marca (o corrige) el ganador; las apuestas se pagan desde el servicio. */
    public void resolver(Candidato candidato) {
        if (estado == EstadoMercado.ANULADO) {
            throw new IllegalStateException("Un mercado anulado no puede tener ganador");
        }
        if (!candidatos.contains(candidato)) {
            throw new IllegalArgumentException(candidato.getNombre() + " no es candidato de " + nombre);
        }
        ganador = candidato;
        estado = EstadoMercado.RESUELTO;
    }

    public void anular() {
        if (estado == EstadoMercado.RESUELTO || estado == EstadoMercado.ANULADO) {
            throw new IllegalStateException("No se puede anular un mercado resuelto o ya anulado");
        }
        estado = EstadoMercado.ANULADO;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public LocalDateTime getCierre() {
        return cierre;
    }

    public EstadoMercado getEstado() {
        return estado;
    }

    public List<Candidato> getCandidatos() {
        return Collections.unmodifiableList(candidatos);
    }

    public Candidato getGanador() {
        return ganador;
    }
}

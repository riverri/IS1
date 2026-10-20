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
 * Entidad JPA: mercado de apuesta a largo plazo con varios candidatos: Balón de Oro, campeón de liga…
 * (HU-44, HU-45). El creador fija la cuota de cada candidato, lo cierra y marca el ganador.
 * Sus estados están en {@link EstadoMercado}. El mercado guarda sus reglas (qué se puede hacer en cada
 * estado); el pago de las apuestas lo hace {@code ResolucionMercadosService}.
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

    /**
     * Los candidatos se guardan y se borran junto con el mercado (cascada). Se cargan siempre con él (EAGER),
     * ordenados de la cuota más baja (el favorito) a la más alta.
     */
    @OneToMany(mappedBy = "mercado", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("cuota, nombre")
    private List<Candidato> candidatos = new ArrayList<>();

    /** Candidato ganador; null hasta que el creador lo marca. */
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

    /**
     * Añade un candidato con su cuota. Solo con el mercado abierto: si estuviera cerrado, nadie podría
     * apostarle.
     *
     * @throws IllegalStateException    si el mercado no está abierto
     * @throws IllegalArgumentException si el nombre está vacío, pasa de 100 caracteres, ya es candidato
     *                                  (sin distinguir mayúsculas) o la cuota no es válida
     */
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

    /**
     * Se puede apostar si está abierto y no ha llegado la fecha de cierre. Al llegar esa fecha deja de admitir
     * apuestas aunque su estado siga siendo ABIERTO: no hace falta que nadie lo cierre a mano (CA-44.3).
     */
    public boolean admiteApuestas(LocalDateTime ahora) {
        return estado == EstadoMercado.ABIERTO && cierre.isAfter(ahora);
    }

    /**
     * Deja de admitir apuestas antes de la fecha de cierre (CA-45.2).
     *
     * @throws IllegalStateException si no está abierto
     */
    public void cerrar() {
        if (estado != EstadoMercado.ABIERTO) {
            throw new IllegalStateException("Solo se puede cerrar un mercado abierto");
        }
        estado = EstadoMercado.CERRADO;
    }

    /**
     * Marca (o corrige) el ganador; las apuestas se pagan desde el servicio (CA-45.3).
     * Se admite en cualquier estado salvo anulado: también con el mercado abierto y, para corregir un error,
     * con el mercado ya resuelto.
     *
     * @throws IllegalStateException    si el mercado está anulado
     * @throws IllegalArgumentException si el candidato no es de este mercado
     */
    public void resolver(Candidato candidato) {
        if (estado == EstadoMercado.ANULADO) {
            throw new IllegalStateException("Un mercado anulado no puede tener ganador");
        }
        // Candidato no redefine equals: se busca el mismo objeto, cargado en la misma transacción
        if (!candidatos.contains(candidato)) {
            throw new IllegalArgumentException(candidato.getNombre() + " no es candidato de " + nombre);
        }
        ganador = candidato;
        estado = EstadoMercado.RESUELTO;
    }

    /**
     * Anula el mercado; el importe de sus apuestas se devuelve desde el servicio. Uno resuelto ya no se anula
     * porque sus apuestas están pagadas: si el ganador estaba mal, se corrige con {@link #resolver}.
     *
     * @throws IllegalStateException si ya está resuelto o anulado
     */
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

    /** Lista de solo lectura: los candidatos se añaden con {@link #anadirCandidato}, que aplica las reglas. */
    public List<Candidato> getCandidatos() {
        return Collections.unmodifiableList(candidatos);
    }

    public Candidato getGanador() {
        return ganador;
    }
}

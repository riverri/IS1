package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
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
 * Apuesta de un usuario: una selección (simple) o varias de eventos distintos (combinada, HU-28).
 * La cuota total es el producto de las cuotas de las selecciones (HU-29).
 */
@Entity
public class Apuesta {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "apuesta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id")
    private List<Seleccion> selecciones = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    /** Producto de las cuotas de todas las selecciones en el momento de apostar. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cuota;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoApuesta estado = EstadoApuesta.ACTIVA;

    /** Lo que se le ha abonado al usuario por esta apuesta (ganancia o devolución). Permite corregir. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal pagado = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Apuesta() {
        // requerido por JPA
    }

    public Apuesta(Usuario usuario, BigDecimal importe, LocalDateTime fecha) {
        this.usuario = usuario;
        this.importe = importe;
        this.fecha = fecha;
        this.cuota = BigDecimal.ONE;
    }

    /** Añade una selección; no se admiten dos del mismo evento (HU-28). */
    public void anadir(Evento evento, Resultado pronostico, BigDecimal cuotaSeleccion) {
        if (selecciones.stream().anyMatch(s -> s.getEvento().getId().equals(evento.getId()))) {
            throw new IllegalArgumentException("No se pueden combinar dos selecciones del mismo evento");
        }
        selecciones.add(new Seleccion(this, evento, pronostico, cuotaSeleccion));
        cuota = producto(selecciones.stream().map(Seleccion::getCuota).toList());
    }

    /** Resultado de uno de sus eventos: se resuelve la selección y se recalcula la apuesta (HU-25, HU-30). */
    public void resolver(Evento evento, Resultado resultado) {
        if (estado == EstadoApuesta.CANCELADA) {
            return;
        }
        seleccionDe(evento).resolver(resultado);
        reevaluar();
    }

    /** Uno de sus eventos se ha anulado: esa selección cuenta con cuota 1,00 (HU-05, HU-30). */
    public void anular(Evento evento) {
        if (estado == EstadoApuesta.CANCELADA) {
            return;
        }
        seleccionDe(evento).anular();
        reevaluar();
    }

    /**
     * Estado según sus selecciones: perdida en cuanto falla una; ganada cuando todas están acertadas
     * o anuladas; anulada si se anulan todas. Ajusta el saldo con la diferencia respecto a lo ya pagado,
     * así una corrección de resultado deshace pagos anteriores (HU-04).
     */
    private void reevaluar() {
        boolean algunaFallada = selecciones.stream().anyMatch(s -> s.getEstado() == EstadoSeleccion.FALLADA);
        boolean algunaPendiente = selecciones.stream().anyMatch(s -> s.getEstado() == EstadoSeleccion.PENDIENTE);
        boolean todasAnuladas = selecciones.stream().allMatch(s -> s.getEstado() == EstadoSeleccion.ANULADA);

        if (algunaFallada) {
            estado = EstadoApuesta.PERDIDA;
        } else if (algunaPendiente) {
            estado = EstadoApuesta.ACTIVA;
        } else if (todasAnuladas) {
            estado = EstadoApuesta.ANULADA;
        } else {
            estado = EstadoApuesta.GANADA;
        }

        BigDecimal debePagar = switch (estado) {
            case GANADA -> getGananciaPotencial();
            case ANULADA -> importe;
            default -> BigDecimal.ZERO;
        };
        usuario.ajustar(debePagar.subtract(pagado));
        pagado = debePagar;
    }

    /** Se puede cancelar mientras esté activa y ninguno de sus eventos haya empezado (HU-26). */
    public boolean cancelable(LocalDateTime ahora) {
        return estado == EstadoApuesta.ACTIVA && selecciones.stream()
                .allMatch(s -> s.getEvento().getEstado() == EstadoEvento.PROGRAMADO
                        && s.getEvento().getFechaHora().isAfter(ahora));
    }

    /** Cancela la apuesta y devuelve el importe al usuario. */
    public void cancelar(LocalDateTime ahora) {
        if (!cancelable(ahora)) {
            throw new IllegalStateException("La apuesta ya no se puede cancelar");
        }
        estado = EstadoApuesta.CANCELADA;
        usuario.abonar(importe);
    }

    /** Cuota que se paga: producto de las cuotas, con 1,00 para las selecciones anuladas. */
    public BigDecimal getCuotaEfectiva() {
        return producto(selecciones.stream().map(Seleccion::getCuotaEfectiva).toList());
    }

    /** Lo que cobra el usuario si acierta: importe × cuota (HU-29). */
    public BigDecimal getGananciaPotencial() {
        return importe.multiply(getCuotaEfectiva()).setScale(2, RoundingMode.DOWN);
    }

    public boolean isResuelta() {
        return estado == EstadoApuesta.GANADA || estado == EstadoApuesta.PERDIDA;
    }

    public boolean isCombinada() {
        return selecciones.size() > 1;
    }

    /** Beneficio neto de una apuesta resuelta: ganancia − importe si se acierta, −importe si no. */
    public BigDecimal getBeneficio() {
        return switch (estado) {
            case GANADA -> getGananciaPotencial().subtract(importe);
            case PERDIDA -> importe.negate();
            default -> BigDecimal.ZERO;
        };
    }

    private Seleccion seleccionDe(Evento evento) {
        return selecciones.stream().filter(s -> s.getEvento().getId().equals(evento.getId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La apuesta no incluye ese evento"));
    }

    static BigDecimal producto(List<BigDecimal> cuotas) {
        return cuotas.stream().reduce(BigDecimal.ONE, BigDecimal::multiply).setScale(2, RoundingMode.DOWN);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public List<Seleccion> getSelecciones() {
        return Collections.unmodifiableList(selecciones);
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public BigDecimal getCuota() {
        return cuota;
    }

    public EstadoApuesta getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

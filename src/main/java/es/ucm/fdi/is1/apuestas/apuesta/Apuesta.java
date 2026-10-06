package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.mercados.Candidato;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;
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
 * Las apuestas a largo plazo (HU-44) tienen una sola selección y no se combinan.
 */
@Entity
public class Apuesta {

    /**
     * Cuota total máxima de una combinada. Sin tope, 10 selecciones de cuota 50 darían una cuota de 50^10:
     * no cabe en la base de datos y un acierto improbable descuadraría el ranking.
     */
    public static final BigDecimal CUOTA_TOTAL_MAXIMA = new BigDecimal("1000");

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
        anadir(evento, new Seleccion(this, evento, pronostico, cuotaSeleccion));
    }

    /** Doble oportunidad, goles o ambos marcan (HU-52); también una sola por evento. */
    public void anadir(Evento evento, Especial especial, BigDecimal cuotaSeleccion) {
        anadir(evento, new Seleccion(this, evento, especial, cuotaSeleccion));
    }

    private void anadir(Evento evento, Seleccion nueva) {
        if (selecciones.stream().anyMatch(Seleccion::isLargoPlazo)) {
            throw new IllegalArgumentException("Las apuestas a largo plazo no se pueden combinar");
        }
        if (selecciones.stream().anyMatch(s -> s.esDe(evento))) {
            throw new IllegalArgumentException("No se pueden combinar dos selecciones del mismo evento");
        }
        selecciones.add(nueva);
        cuota = producto(selecciones.stream().map(Seleccion::getCuota).toList());
    }

    /** Apuesta a largo plazo (HU-44): un candidato de un mercado, sin combinar con nada más. */
    public void anadir(Candidato candidato, BigDecimal cuotaCandidato) {
        if (!selecciones.isEmpty()) {
            throw new IllegalArgumentException("Las apuestas a largo plazo no se pueden combinar");
        }
        selecciones.add(new Seleccion(this, candidato, cuotaCandidato));
        cuota = producto(List.of(cuotaCandidato));
    }

    /** El creador ha marcado (o corregido) el ganador del mercado (HU-45). */
    public void resolver(Mercado mercado, Candidato ganador) {
        if (estado == EstadoApuesta.CANCELADA) {
            return;
        }
        seleccionDe(mercado).resolver(ganador);
        reevaluar();
    }

    /** El mercado se ha anulado: se devuelve el importe. */
    public void anular(Mercado mercado) {
        if (estado == EstadoApuesta.CANCELADA) {
            return;
        }
        seleccionDe(mercado).anular();
        reevaluar();
    }

    /** Resultado de uno de sus eventos: se resuelve la selección y se recalcula la apuesta (HU-25, HU-30). */
    public void resolver(Evento evento, Resultado resultado) {
        if (estado == EstadoApuesta.CANCELADA) {
            return;
        }
        seleccionDe(evento).resolver(resultado, evento.getGolesLocal(), evento.getGolesVisitante());
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

    /**
     * Se puede cancelar mientras esté activa y ninguno de sus eventos haya empezado (HU-26),
     * o, a largo plazo, mientras el mercado admita apuestas.
     */
    public boolean cancelable(LocalDateTime ahora) {
        return estado == EstadoApuesta.ACTIVA && selecciones.stream().allMatch(s -> s.admiteCancelacion(ahora));
    }

    /** Cancela la apuesta y devuelve el importe al usuario. */
    public void cancelar(LocalDateTime ahora) {
        if (!cancelable(ahora)) {
            throw new IllegalStateException("La apuesta ya no se puede cancelar");
        }
        estado = EstadoApuesta.CANCELADA;
        usuario.abonar(importe);
    }

    /**
     * Cambia el importe mientras se pueda cancelar (HU-27): se cobra o se devuelve la diferencia
     * y se aplican las cuotas actuales, una por selección y en el mismo orden.
     */
    public void cambiarImporte(BigDecimal nuevo, List<BigDecimal> cuotasActuales, LocalDateTime ahora) {
        if (!cancelable(ahora)) {
            throw new IllegalStateException("La apuesta ya no se puede modificar");
        }
        BigDecimal diferencia = nuevo.subtract(importe);
        if (diferencia.signum() > 0) {
            usuario.cargar(diferencia);
        } else if (diferencia.signum() < 0) {
            usuario.abonar(diferencia.negate());
        }
        importe = nuevo;
        for (int i = 0; i < selecciones.size(); i++) {
            selecciones.get(i).actualizarCuota(cuotasActuales.get(i));
        }
        cuota = producto(selecciones.stream().map(Seleccion::getCuota).toList());
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

    public boolean isLargoPlazo() {
        return selecciones.stream().anyMatch(Seleccion::isLargoPlazo);
    }

    private Seleccion seleccionDe(Evento evento) {
        return selecciones.stream().filter(s -> s.esDe(evento)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La apuesta no incluye ese evento"));
    }

    private Seleccion seleccionDe(Mercado mercado) {
        return selecciones.stream().filter(s -> s.esDe(mercado)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La apuesta no incluye ese mercado"));
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

    /** Lo abonado al usuario por esta apuesta: ganancia (con el importe) o devolución. */
    public BigDecimal getPagado() {
        return pagado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

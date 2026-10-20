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
 *
 * <p>Entidad JPA: guarda el importe, la cuota total, el estado y lo ya pagado al usuario. Sus
 * {@link Seleccion}es se guardan y se borran con ella (cascade). El estado se recalcula cada vez que se
 * resuelve o se anula una selección ({@code reevaluar}), y aquí se hacen los pagos, devoluciones y
 * cambios de importe sobre el saldo del {@link Usuario}. El cobro inicial lo hace {@link ApuestaService}.
 */
@Entity
public class Apuesta {

    /**
     * Cuota total máxima de una combinada. Sin tope, 10 selecciones de cuota 50 darían una cuota de 50^10:
     * no cabe en la base de datos y un acierto improbable descuadraría el ranking.
     * Se comprueba en {@link ApuestaService} antes de apostar y al añadir al boleto.
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

    /** Moneditas apostadas: se descuentan del saldo al apostar. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    /**
     * Producto de las cuotas de todas las selecciones en el momento de apostar, redondeado hacia abajo
     * a 2 decimales. Se recalcula si el usuario cambia el importe (HU-27).
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cuota;

    /** Empieza activa; cambia al resolverse o anularse sus selecciones, o al cancelarla el usuario. */
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

    /** Apuesta vacía; la cuota empieza en 1,00 y se multiplica con cada selección que se añade. */
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

    /** Lanza IllegalArgumentException si ya hay una a largo plazo o una del mismo evento. */
    private void anadir(Evento evento, Seleccion nueva) {
        if (selecciones.stream().anyMatch(Seleccion::isLargoPlazo)) {
            throw new IllegalArgumentException("Las apuestas a largo plazo no se pueden combinar");
        }
        if (selecciones.stream().anyMatch(s -> s.esDe(evento))) {
            throw new IllegalArgumentException("No se pueden combinar dos selecciones del mismo evento");
        }
        selecciones.add(nueva);
        // Multiplicador total (HU-29): producto de las cuotas de todas las selecciones
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
        // Una apuesta cancelada ya devolvió el importe: no se resuelve (igual en los otros resolver y anular)
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

        // El orden importa: una fallada pierde la combinada aunque queden otras pendientes (CA-30.1)
        if (algunaFallada) {
            estado = EstadoApuesta.PERDIDA;
        } else if (algunaPendiente) {
            estado = EstadoApuesta.ACTIVA;
        } else if (todasAnuladas) {
            estado = EstadoApuesta.ANULADA;
        } else {
            // Acertadas, o acertadas y anuladas: las anuladas cuentan con cuota 1,00 (CA-30.3)
            estado = EstadoApuesta.GANADA;
        }

        // Lo que le corresponde al usuario en el estado actual; la ganancia ya incluye el importe apostado
        BigDecimal debePagar = switch (estado) {
            case GANADA -> getGananciaPotencial();
            case ANULADA -> importe;
            default -> BigDecimal.ZERO;
        };
        // Solo se abona la diferencia con lo pagado antes. Si se corrige un resultado y una ganada pasa a
        // perdida, la diferencia es negativa y se le retira lo cobrado (el saldo puede quedar negativo)
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

    /**
     * Cancela la apuesta y devuelve el importe al usuario (HU-26).
     *
     * @throws IllegalStateException si ya no es cancelable (ver {@link #cancelable})
     */
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
     *
     * @throws IllegalStateException si ya no se puede cancelar
     * @throws es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException si sube el importe y no hay saldo
     */
    public void cambiarImporte(BigDecimal nuevo, List<BigDecimal> cuotasActuales, LocalDateTime ahora) {
        if (!cancelable(ahora)) {
            throw new IllegalStateException("La apuesta ya no se puede modificar");
        }
        // Sube: se cobra solo la diferencia. Baja: se devuelve la diferencia (CA-27.1, CA-27.2)
        BigDecimal diferencia = nuevo.subtract(importe);
        if (diferencia.signum() > 0) {
            usuario.cargar(diferencia);
        } else if (diferencia.signum() < 0) {
            usuario.abonar(diferencia.negate());
        }
        importe = nuevo;
        // La cuota vista al apostar ya no vale: toda la apuesta pasa a la cuota vigente ahora
        for (int i = 0; i < selecciones.size(); i++) {
            selecciones.get(i).actualizarCuota(cuotasActuales.get(i));
        }
        cuota = producto(selecciones.stream().map(Seleccion::getCuota).toList());
    }

    /** Cuota que se paga: producto de las cuotas, con 1,00 para las selecciones anuladas. */
    public BigDecimal getCuotaEfectiva() {
        return producto(selecciones.stream().map(Seleccion::getCuotaEfectiva).toList());
    }

    /**
     * Lo que cobra el usuario si acierta: importe × cuota (HU-29), con el importe incluido.
     * Se redondea hacia abajo para no pagar nunca un céntimo de más.
     */
    public BigDecimal getGananciaPotencial() {
        return importe.multiply(getCuotaEfectiva()).setScale(2, RoundingMode.DOWN);
    }

    /** Ganada o perdida. Las anuladas y canceladas no cuentan en las estadísticas (HU-35). */
    public boolean isResuelta() {
        return estado == EstadoApuesta.GANADA || estado == EstadoApuesta.PERDIDA;
    }

    public boolean isCombinada() {
        return selecciones.size() > 1;
    }

    /** Beneficio neto de una apuesta resuelta: ganancia − importe si se acierta, −importe si no; 0 en otro estado. */
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

    /**
     * Producto de varias cuotas redondeado hacia abajo a 2 decimales (1,00 si no hay ninguna).
     * Lo usan también los servicios para calcular la cuota total antes de apostar.
     */
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

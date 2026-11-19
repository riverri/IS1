package es.ucm.fdi.is1.apuestas.quiniela;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Version;

import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;

/**
 * Entidad JPA: quiniela de 3 a 15 partidos (HU-78). No tiene cuotas: los jugadores echan un boleto con 1, X o 2 en
 * cada partido, pagan el mismo precio y el bote (lo pagado menos la comisión de la casa) se reparte entre los que más
 * aciertan (HU-80). {@code @Version} evita que se reparta o se anule dos veces a la vez.
 */
@Entity
public class Quiniela {

    public static final int MIN_PARTIDOS = 3;
    public static final int MAX_PARTIDOS = 15;
    /** Parte de lo pagado que se queda la casa: el 10 %. */
    public static final BigDecimal COMISION = new BigDecimal("0.10");

    @Version
    private long version;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** Precio de cada boleto. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal comision;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoQuiniela estado = EstadoQuiniela.ABIERTA;

    @Column(nullable = false)
    private LocalDateTime creada;

    @OneToMany(mappedBy = "quiniela", cascade = CascadeType.ALL)
    @OrderBy("orden")
    private List<PartidoQuiniela> partidos = new ArrayList<>();

    protected Quiniela() {
        // requerido por JPA
    }

    /** @param eventos ya comprobados y en orden de fecha ({@link QuinielaService#crear}) */
    Quiniela(String nombre, BigDecimal precio, List<Evento> eventos, LocalDateTime creada) {
        this.nombre = nombre;
        this.precio = precio;
        this.comision = COMISION;
        this.creada = creada;
        for (int i = 0; i < eventos.size(); i++) {
            partidos.add(new PartidoQuiniela(this, eventos.get(i), i));
        }
    }

    /**
     * Admite boletos: abierta, sin ningún partido empezado y todos programados (CA-79.2). Un partido suspendido o
     * anulado la cierra.
     */
    public boolean admiteBoletos(LocalDateTime ahora) {
        return estado == EstadoQuiniela.ABIERTA && partidos.stream().map(PartidoQuiniela::getEvento)
                .allMatch(e -> e.getEstado() == EstadoEvento.PROGRAMADO && !e.haEmpezado(ahora));
    }

    /** Todos los partidos tienen resultado o están anulados: ya se puede repartir (CA-80.1). */
    public boolean isListaParaRepartir() {
        return estado == EstadoQuiniela.ABIERTA && partidos.stream().allMatch(PartidoQuiniela::isJugado);
    }

    /** Lo que se reparte de lo pagado: se quita la comisión y se redondea hacia abajo. */
    public BigDecimal bote(BigDecimal pagado) {
        return pagado.multiply(BigDecimal.ONE.subtract(comision)).setScale(2, RoundingMode.DOWN);
    }

    void repartida() {
        estado = EstadoQuiniela.REPARTIDA;
    }

    void anular() {
        estado = EstadoQuiniela.ANULADA;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public EstadoQuiniela getEstado() {
        return estado;
    }

    public LocalDateTime getCreada() {
        return creada;
    }

    public List<PartidoQuiniela> getPartidos() {
        return partidos;
    }

    /** Fecha del primer partido: a partir de ahí ya no se admiten boletos. */
    public LocalDateTime getCierre() {
        // El más temprano de todos, no el primero de la lista: si se aplaza un partido, el orden de la quiniela no cambia
        return partidos.stream().map(p -> p.getEvento().getFechaHora()).min(Comparator.naturalOrder()).orElse(null);
    }
}

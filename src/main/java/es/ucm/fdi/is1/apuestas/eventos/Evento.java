package es.ucm.fdi.is1.apuestas.eventos;

import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

/**
 * Partido entre dos equipos de una competición, sobre el que se apuesta (G/E/P).
 *
 * <p>Entidad JPA y pieza central del dominio: las apuestas, las cuotas y las estadísticas de equipos
 * se calculan a partir de ella. Lo crea el creador de apuestas a mano o la sincronización con la API
 * (HU-21).
 *
 * <p>Guarda su {@link EstadoEvento} y solo permite las transiciones válidas; los métodos que cambian
 * el estado lanzan {@link IllegalStateException} si no se puede. El efecto sobre las apuestas
 * (resolverlas, devolver el dinero) no se hace aquí, sino en {@code ResolucionService}.
 * El diagrama de estados está en {@link EstadoEvento}.
 *
 * <p>Por qué no hay setters para todo: si cualquiera pudiera hacer {@code evento.setEstado(FINALIZADO)}, se podría
 * saltar las reglas (por ejemplo, dar resultado a un partido anulado). Por eso el estado solo cambia con métodos
 * con nombre ({@link #finalizar}, {@link #suspender}, {@link #anular}...), que comprueban antes si se puede.
 */
@Entity
public class Evento {

    @Id
    @GeneratedValue
    private Long id;

    // @NotNull (Bean Validation) se comprueba antes de guardar; @ManyToOne(optional = false) hace la columna
    // obligatoria en la base de datos. Cada evento apunta a una competición y a dos equipos
    @NotNull
    @ManyToOne(optional = false)
    private Competicion competicion;

    @NotNull
    @ManyToOne(optional = false)
    private Equipo local;

    @NotNull
    @ManyToOne(optional = false)
    private Equipo visitante;

    @NotNull
    private LocalDateTime fechaHora;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoEvento estado = EstadoEvento.PROGRAMADO;

    /** Resultado final, una vez que el creador lo introduce (HU-04). */
    @Enumerated(EnumType.STRING)
    private Resultado resultado;

    /** Marcador final, si se conoce (HU-52). Sin él, las apuestas de goles se anulan. */
    // Integer y no int: un int no puede ser null, y aquí null significa "marcador desconocido"
    private Integer golesLocal;

    private Integer golesVisitante;

    /** Identificador del partido en la API de datos deportivos. */
    @Column(unique = true)
    private Long idExterno;

    /** Jornada, ronda o gran premio ("Jornada 8", "Cuartos de final"…). Opcional. */
    private String fase;

    /** Lo suspendió la sincronización con la API (aplazado); si lo suspendió el creador, la API no lo reactiva. */
    private boolean suspendidoPorApi;

    protected Evento() {
        // requerido por JPA
    }

    /** Evento nuevo, en estado programado y sin resultado. */
    public Evento(Competicion competicion, Equipo local, Equipo visitante, LocalDateTime fechaHora) {
        this.competicion = competicion;
        this.local = local;
        this.visitante = visitante;
        this.fechaHora = fechaHora;
    }

    /** Solo se puede apostar a eventos programados que todavía no han empezado (HU-19). */
    public boolean admiteApuestas(LocalDateTime ahora) {
        return estado == EstadoEvento.PROGRAMADO && fechaHora.isAfter(ahora);
    }

    /**
     * Fija (o corrige) el resultado final y marca el evento como finalizado (HU-04).
     * Vale desde programado, suspendido o finalizado (para corregir). Borra el marcador: con un resultado
     * sin goles, las apuestas de goles se anulan.
     *
     * @throws IllegalStateException    si el evento está anulado
     * @throws IllegalArgumentException si es empate en un deporte que no lo admite
     */
    public void finalizar(Resultado resultadoFinal) {
        if (estado == EstadoEvento.ANULADO) {
            throw new IllegalStateException("Un evento anulado no puede tener resultado");
        }
        if (resultadoFinal == Resultado.EMPATE && !getDeporte().isAdmiteEmpate()) {
            throw new IllegalArgumentException("En " + getDeporte().getNombre() + " no hay empate");
        }
        resultado = resultadoFinal;
        golesLocal = null;
        golesVisitante = null;
        estado = EstadoEvento.FINALIZADO;
    }

    /**
     * Fija (o corrige) el marcador final; el resultado sale de él (HU-52). Solo en fútbol.
     *
     * @throws IllegalArgumentException si algún marcador es negativo o el deporte no admite empate
     * @throws IllegalStateException    si el evento está anulado
     */
    public void finalizarConMarcador(int local, int visitante) {
        if (local < 0 || visitante < 0) {
            throw new IllegalArgumentException("Los goles no pueden ser negativos");
        }
        // "Admite empate" equivale aquí a "es fútbol": en el resto no hay goles que apuntar
        if (!getDeporte().isAdmiteEmpate()) {
            throw new IllegalArgumentException("En " + getDeporte().getNombre() + " solo se indica el ganador");
        }
        // finalizar() borra los goles, así que se guardan después.
        // Ternario encadenado: si local > visitante → LOCAL; si no, si local < visitante → VISITANTE; si no → EMPATE
        finalizar(local > visitante ? Resultado.LOCAL : local < visitante ? Resultado.VISITANTE : Resultado.EMPATE);
        golesLocal = local;
        golesVisitante = visitante;
    }

    /**
     * Deja de admitir apuestas nuevas hasta que se reactive (HU-05). Las que ya hay siguen activas.
     * Suspendido por el creador: la API no lo reactivará sola.
     *
     * @throws IllegalStateException si no está programado
     */
    public void suspender() {
        if (estado != EstadoEvento.PROGRAMADO) {
            throw new IllegalStateException("Solo se puede suspender un evento programado");
        }
        estado = EstadoEvento.SUSPENDIDO;
        suspendidoPorApi = false;
    }

    /** Aplazado según la API: la propia sincronización lo reactivará cuando vuelva a tener fecha. */
    public void suspenderPorApi() {
        suspender();
        suspendidoPorApi = true;
    }

    public boolean isSuspendidoPorApi() {
        return suspendidoPorApi;
    }

    /**
     * Vuelve a programado y admite apuestas otra vez, si no ha empezado (HU-05).
     *
     * @throws IllegalStateException si no está suspendido
     */
    public void reactivar() {
        if (estado != EstadoEvento.SUSPENDIDO) {
            throw new IllegalStateException("Solo se puede reactivar un evento suspendido");
        }
        estado = EstadoEvento.PROGRAMADO;
        suspendidoPorApi = false;
    }

    /**
     * Anula el evento; las apuestas se devuelven desde el servicio (HU-05). Es definitivo.
     *
     * @throws IllegalStateException si ya está finalizado o anulado
     */
    public void anular() {
        if (estado == EstadoEvento.FINALIZADO || estado == EstadoEvento.ANULADO) {
            throw new IllegalStateException("No se puede anular un evento finalizado o ya anulado");
        }
        estado = EstadoEvento.ANULADO;
    }

    /** True a partir de la hora de inicio, sea cual sea el estado. */
    public boolean haEmpezado(LocalDateTime ahora) {
        // "no es posterior a ahora" = es ahora mismo o ya ha pasado
        return !fechaHora.isAfter(ahora);
    }

    public Resultado getResultado() {
        return resultado;
    }

    public Integer getGolesLocal() {
        return golesLocal;
    }

    public Integer getGolesVisitante() {
        return golesVisitante;
    }

    /** True si se finalizó con marcador (HU-52) y no solo con el ganador. */
    public boolean isConMarcador() {
        return golesLocal != null && golesVisitante != null;
    }

    /** El evento no guarda el deporte: es el de su competición. */
    public Deporte getDeporte() {
        return competicion.getDeporte();
    }

    public Long getId() {
        return id;
    }

    public Competicion getCompeticion() {
        return competicion;
    }

    public Equipo getLocal() {
        return local;
    }

    public Equipo getVisitante() {
        return visitante;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    /**
     * El creador corrige un evento mal dado de alta (HU-46). Solo antes de que empiece y si no está
     * finalizado ni anulado; las comprobaciones de equipos y apuestas las hace el servicio.
     *
     * @throws IllegalStateException si el evento no es {@link #editable(LocalDateTime) editable}
     */
    public void modificar(Equipo nuevoLocal, Equipo nuevoVisitante, LocalDateTime nuevaFecha, String nuevaFase,
                          LocalDateTime ahora) {
        if (!editable(ahora)) {
            throw new IllegalStateException("Solo se puede editar un evento que aún no ha empezado");
        }
        local = nuevoLocal;
        visitante = nuevoVisitante;
        fechaHora = nuevaFecha;
        fase = nuevaFase;
    }

    /** Programado o suspendido y sin empezar (HU-46). */
    public boolean editable(LocalDateTime ahora) {
        return (estado == EstadoEvento.PROGRAMADO || estado == EstadoEvento.SUSPENDIDO) && fechaHora.isAfter(ahora);
    }

    /**
     * La fuente de datos ha cambiado la fecha del partido (solo si aún no se ha jugado).
     * En un evento finalizado o anulado no hace nada, sin lanzar excepción.
     */
    public void cambiarFecha(LocalDateTime nuevaFecha) {
        if (estado == EstadoEvento.PROGRAMADO || estado == EstadoEvento.SUSPENDIDO) {
            fechaHora = nuevaFecha;
        }
    }

    public Long getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(Long idExterno) {
        this.idExterno = idExterno;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }
}

package es.ucm.fdi.is1.apuestas.anuncios;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

/**
 * Entidad JPA: anuncio interno que promociona otra sección de la web y lleva a ella al pulsarlo (HU-60).
 * Lo crea y lo edita el creador de apuestas desde Gestión (HU-61); no hay nada escrito en las plantillas.
 *
 * <p>Las reglas de cuándo se puede mostrar están aquí ({@link #seMuestra}); cuál se elige entre los posibles
 * lo decide {@link AnuncioService}. Las veces que se ha mostrado se cuentan en {@link #impresiones}; los clics,
 * uno por fila, en {@link ClicAnuncio} (HU-62).
 */
@Entity
public class Anuncio {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 60)
    private String titulo;

    @Column(nullable = false, length = 160)
    private String texto;

    /** Emoji que acompaña al anuncio; opcional. */
    @Column(length = 8)
    private String icono;

    /** Ruta interna a la que lleva, por ejemplo "/mercados". Siempre de una {@link Ubicacion} conocida. */
    @Column(nullable = false, length = 200)
    private String destino;

    /**
     * Secciones en las que se muestra. {@code @ElementCollection}: una lista de valores simples (no de entidades),
     * que JPA guarda en su propia tabla, {@code anuncio_ubicacion}, con una fila por sección.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "anuncio_ubicacion", joinColumns = @JoinColumn(name = "anuncio_id"))
    @Column(name = "ubicacion", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Ubicacion> ubicaciones = new HashSet<>();

    /** De 1 a 10: en cada página sale uno de los de prioridad más alta, al azar entre ellos (CA-60.1). */
    @Column(nullable = false)
    private int prioridad;

    @Column(nullable = false)
    private boolean activo = true;

    /** Desde y hasta cuándo se muestra; null = sin límite. */
    private LocalDateTime desde;

    private LocalDateTime hasta;

    /** Veces que se ha mostrado (HU-62). Lo incrementa una consulta de {@link AnuncioRepository}. */
    @Column(nullable = false)
    private long impresiones;

    protected Anuncio() {
        // requerido por JPA
    }

    /** Anuncio nuevo y activo; mismas comprobaciones que {@link #cambiar}. */
    public Anuncio(String titulo, String texto, String icono, String destino, Set<Ubicacion> ubicaciones,
                   int prioridad, LocalDateTime desde, LocalDateTime hasta) {
        cambiar(titulo, texto, icono, destino, ubicaciones, prioridad, desde, hasta);
    }

    /**
     * Cambia todos los datos editables (HU-61).
     *
     * @throws IllegalArgumentException si el destino no es una sección de la web, no hay ubicaciones, la prioridad
     *                                  no está entre 1 y 10 o "hasta" es anterior a "desde"
     */
    public void cambiar(String nuevoTitulo, String nuevoTexto, String nuevoIcono, String nuevoDestino,
                        Set<Ubicacion> nuevasUbicaciones, int nuevaPrioridad, LocalDateTime nuevoDesde,
                        LocalDateTime nuevoHasta) {
        // CA-61.2: solo rutas de la propia web, y de una sección que exista
        if (Ubicacion.de(nuevoDestino).isEmpty()) {
            throw new IllegalArgumentException("El destino tiene que ser una sección de la web, como /mercados o /ranking");
        }
        if (nuevasUbicaciones == null || nuevasUbicaciones.isEmpty()) {
            throw new IllegalArgumentException("Elige al menos una sección donde mostrarlo");
        }
        if (nuevaPrioridad < 1 || nuevaPrioridad > 10) {
            throw new IllegalArgumentException("La prioridad va de 1 a 10");
        }
        if (nuevoDesde != null && nuevoHasta != null && nuevoHasta.isBefore(nuevoDesde)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
        }
        titulo = nuevoTitulo.trim();
        texto = nuevoTexto.trim();
        icono = nuevoIcono == null || nuevoIcono.isBlank() ? null : nuevoIcono.trim();
        destino = nuevoDestino.trim();
        ubicaciones = EnumSet.copyOf(nuevasUbicaciones);
        prioridad = nuevaPrioridad;
        desde = nuevoDesde;
        hasta = nuevoHasta;
    }

    /**
     * Si se puede mostrar en una página de {@code donde} (CA-60.2, CA-60.3): activo, dentro de sus fechas, puesto en
     * esa sección, que no lleve a la propia sección y, si su destino necesita sesión, solo a quien la tiene.
     */
    public boolean seMuestra(Ubicacion donde, boolean conSesion, LocalDateTime ahora) {
        Optional<Ubicacion> seccionDestino = Ubicacion.de(destino);
        return activo
                && (desde == null || !ahora.isBefore(desde))
                && (hasta == null || ahora.isBefore(hasta))
                && ubicaciones.contains(donde)
                && seccionDestino.isPresent() && seccionDestino.get() != donde
                && (conSesion || !seccionDestino.get().isRequiereSesion());
    }

    public void activar(boolean activar) {
        activo = activar;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTexto() {
        return texto;
    }

    public String getIcono() {
        return icono;
    }

    public String getDestino() {
        return destino;
    }

    /** Copia en el orden del enum, para mostrarlas siempre igual. */
    public Set<Ubicacion> getUbicaciones() {
        return ubicaciones.isEmpty() ? EnumSet.noneOf(Ubicacion.class) : EnumSet.copyOf(ubicaciones);
    }

    public int getPrioridad() {
        return prioridad;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getDesde() {
        return desde;
    }

    public LocalDateTime getHasta() {
        return hasta;
    }

    public long getImpresiones() {
        return impresiones;
    }
}

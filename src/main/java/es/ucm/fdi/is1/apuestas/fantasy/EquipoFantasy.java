package es.ucm.fdi.is1.apuestas.fantasy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Version;

/**
 * El equipo de un usuario en la liga fantasy (HU-81 a HU-83): siete jugadores reales de fútbol (un portero, dos
 * defensas, dos centrocampistas y dos delanteros) fichados con un presupuesto ficticio de {@link #PRESUPUESTO}
 * millones, como mucho {@link #MAXIMO_POR_CLUB} del mismo club. Suma puntos con los partidos de sus equipos (ver
 * {@link PuntosFantasy}). Es gratis: no se juegan moneditas.
 */
@Entity
public class EquipoFantasy {

    public static final BigDecimal PRESUPUESTO = new BigDecimal("90.0");
    public static final int MAXIMO_POR_CLUB = 2;
    /** Cuántos jugadores de cada posición lleva el equipo, en el orden de la alineación. */
    public static final Map<Posicion, Integer> FORMACION = Collections.unmodifiableMap(new EnumMap<>(Map.of(
            Posicion.PORTERO, 1, Posicion.DEFENSA, 2, Posicion.CENTROCAMPISTA, 2, Posicion.DELANTERO, 2)));

    @Version
    private long version;

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 40)
    private String nombre;

    @Column(nullable = false)
    private LocalDateTime creado;

    /** Todos los fichajes, también los ya vendidos (sus puntos siguen contando); se cargan al abrir este equipo. */
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("desde asc, id asc")
    private List<FichajeFantasy> fichajes = new ArrayList<>();

    protected EquipoFantasy() {
        // requerido por JPA
    }

    /** @throws IllegalArgumentException si no tiene nombre o el usuario es el creador */
    public EquipoFantasy(Usuario usuario, String nombre, LocalDateTime ahora) {
        if (usuario.isCreador()) {
            throw new IllegalArgumentException("El creador de apuestas no juega a la liga fantasy");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Ponle un nombre a tu equipo");
        }
        if (nombre.strip().length() > 40) {
            throw new IllegalArgumentException("El nombre puede tener como mucho 40 caracteres");
        }
        this.usuario = usuario;
        this.nombre = nombre.strip();
        this.creado = ahora;
    }

    /** Precio de un jugador, en millones: sale de su nota y de la calidad de su equipo (de unos 7 a unos 20). */
    public static BigDecimal precio(Jugador jugador) {
        double precio = jugador.getNota() * 1.5 + jugador.getEquipo().getCalidad() * 0.5;
        return BigDecimal.valueOf(precio).setScale(1, RoundingMode.HALF_UP);
    }

    /** Los que están ahora en el equipo, de la portería a la delantera. */
    public List<FichajeFantasy> getPlantilla() {
        return fichajes.stream().filter(FichajeFantasy::isActivo)
                .sorted(Comparator.comparing(FichajeFantasy::getPosicion)
                        .thenComparing(FichajeFantasy::getDesde))
                .toList();
    }

    /** Lo que queda del presupuesto: al vender a un jugador se recupera lo que costó. */
    public BigDecimal getCaja() {
        return PRESUPUESTO.subtract(getPlantilla().stream().map(FichajeFantasy::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public boolean isCompleto() {
        return getPlantilla().size() == FORMACION.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Huecos libres en esa posición. */
    public int huecos(Posicion posicion) {
        long ocupados = getPlantilla().stream().filter(f -> f.getPosicion() == posicion).count();
        return FORMACION.get(posicion) - (int) ocupados;
    }

    /** Por qué no se puede fichar a ese jugador ahora, o null si se puede. Lo usan el mercado y {@link #fichar}. */
    public String motivoParaNoFichar(Jugador jugador) {
        if (jugador.getEquipo().getDeporte() != Deporte.FUTBOL) {
            return "Solo juegan futbolistas";
        }
        if (getPlantilla().stream().anyMatch(f -> f.esDe(jugador.getId()))) {
            return "Ya está en tu equipo";
        }
        if (huecos(jugador.getPosicion()) <= 0) {
            return "No te quedan huecos de " + jugador.getPosicion().getNombre().toLowerCase();
        }
        long delClub = getPlantilla().stream()
                .filter(f -> f.getClub().getId().equals(jugador.getEquipo().getId())).count();
        if (delClub >= MAXIMO_POR_CLUB) {
            return "Ya tienes " + MAXIMO_POR_CLUB + " del " + jugador.getEquipo().getNombre();
        }
        if (precio(jugador).compareTo(getCaja()) > 0) {
            return "No te llega el presupuesto";
        }
        return null;
    }

    /**
     * @param desde desde cuándo suma (ver {@link FantasyService.Jornada#efecto})
     * @throws IllegalArgumentException si no se puede (ver {@link #motivoParaNoFichar})
     */
    FichajeFantasy fichar(Jugador jugador, LocalDateTime desde) {
        String motivo = motivoParaNoFichar(jugador);
        if (motivo != null) {
            throw new IllegalArgumentException(motivo);
        }
        FichajeFantasy fichaje = new FichajeFantasy(this, jugador, precio(jugador), desde);
        fichajes.add(fichaje);
        return fichaje;
    }

    /**
     * Vende a un jugador: se recupera lo que costó, deja su hueco libre y deja de sumar puntos a partir de
     * {@code hasta} (ver {@link FantasyService.Jornada#efecto}).
     *
     * @throws IllegalArgumentException si no está en el equipo
     */
    FichajeFantasy vender(Long jugadorId, LocalDateTime hasta) {
        FichajeFantasy fichaje = getPlantilla().stream().filter(f -> f.esDe(jugadorId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Ese jugador no está en tu equipo"));
        fichaje.vender(hasta);
        return fichaje;
    }

    /**
     * Vende un fichaje por su id: sirve también si el jugador ya no existe.
     *
     * @throws IllegalArgumentException si no está en el equipo
     */
    FichajeFantasy venderFichaje(Long fichajeId, LocalDateTime hasta) {
        FichajeFantasy fichaje = getPlantilla().stream().filter(f -> f.getId().equals(fichajeId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Ese jugador no está en tu equipo"));
        fichaje.vender(hasta);
        return fichaje;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDateTime getCreado() {
        return creado;
    }

    public List<FichajeFantasy> getFichajes() {
        return Collections.unmodifiableList(fichajes);
    }
}

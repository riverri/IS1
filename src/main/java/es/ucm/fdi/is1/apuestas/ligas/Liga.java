package es.ucm.fdi.is1.apuestas.ligas;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

/**
 * Entidad JPA: liga privada entre amigos (HU-51). Tiene un código de invitación para unirse y su propio
 * ranking, calculado solo con sus miembros. Las reglas (cuántas ligas, quién sale o borra) están en
 * {@link LigaService}; por eso unir y salir no son públicos.
 */
@Entity
public class Liga {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 40)
    private String nombre;

    /** Código de invitación de 6 caracteres; único, lo genera {@link LigaService}. */
    @Column(nullable = false, unique = true, length = 6)
    private String codigo;

    /** Quien la creó: es el único que puede borrarla y no puede salir de ella. */
    @ManyToOne(optional = false)
    private Usuario creador;

    @Column(nullable = false)
    private LocalDateTime creada;

    /**
     * Relación muchos a muchos con los usuarios, en la tabla intermedia liga_miembro.
     * Se cargan con la liga: las páginas los muestran fuera de la transacción (open-in-view desactivado).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "liga_miembro", joinColumns = @JoinColumn(name = "liga_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private Set<Usuario> miembros = new HashSet<>();

    protected Liga() {
        // requerido por JPA
    }

    /** Crea la liga con su creador como primer miembro (CA-51.1). */
    public Liga(String nombre, String codigo, Usuario creador, LocalDateTime creada) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.creador = creador;
        this.creada = creada;
        miembros.add(creador);
    }

    /**
     * Compara por id porque Usuario no redefine equals: el mismo usuario cargado dos veces serían dos
     * objetos distintos. Lo mismo en {@link #esCreador} y {@link #salir}.
     */
    public boolean esMiembro(Usuario usuario) {
        return miembros.stream().anyMatch(m -> m.getId().equals(usuario.getId()));
    }

    public boolean esCreador(Usuario usuario) {
        return creador.getId().equals(usuario.getId());
    }

    /** Lo llama {@link LigaService#unirse}, que antes comprueba que no sea ya miembro. */
    void unir(Usuario usuario) {
        miembros.add(usuario);
    }

    void salir(Usuario usuario) {
        miembros.removeIf(m -> m.getId().equals(usuario.getId()));
    }

    /** Ids de los miembros, para filtrar el ranking general y quedarse solo con la liga. */
    public Set<Long> getIdsMiembros() {
        return miembros.stream().map(Usuario::getId).collect(Collectors.toSet());
    }

    /** Miembros que siguen jugando (sin los que se han dado de baja), por nombre. */
    public List<Usuario> getMiembrosActivos() {
        return miembros.stream().filter(u -> !u.isEliminado())
                .sorted(Comparator.comparing(Usuario::getNombre)).toList();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public Usuario getCreador() {
        return creador;
    }

    public LocalDateTime getCreada() {
        return creada;
    }
}

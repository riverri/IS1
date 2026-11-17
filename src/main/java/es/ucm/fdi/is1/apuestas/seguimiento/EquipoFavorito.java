package es.ucm.fdi.is1.apuestas.seguimiento;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Entidad JPA: un equipo que un usuario ha marcado como favorito (HU-86). Uno por usuario y equipo. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "equipo_id"}))
public class EquipoFavorito {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Equipo equipo;

    protected EquipoFavorito() {
        // requerido por JPA
    }

    EquipoFavorito(Usuario usuario, Equipo equipo) {
        this.usuario = usuario;
        this.equipo = equipo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Equipo getEquipo() {
        return equipo;
    }
}

package es.ucm.fdi.is1.apuestas.anuncios;

import java.time.LocalDateTime;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Entidad JPA: un clic en un anuncio, para medir cuáles funcionan (HU-62). Guarda de qué página venía y quién
 * lo pulsó (null si era un visitante). No se modifica nunca.
 */
@Entity
public class ClicAnuncio {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Anuncio anuncio;

    /** Quién pulsó; null si no había iniciado sesión. */
    @ManyToOne
    private Usuario usuario;

    /** Página en la que estaba el anuncio, por ejemplo "/eventos". */
    @Column(nullable = false, length = 200)
    private String origen;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected ClicAnuncio() {
        // requerido por JPA
    }

    ClicAnuncio(Anuncio anuncio, Usuario usuario, String origen, LocalDateTime fecha) {
        this.anuncio = anuncio;
        this.usuario = usuario;
        // La columna admite 200 caracteres: una ruta más larga se guarda cortada
        this.origen = origen.length() > 200 ? origen.substring(0, 200) : origen;
        this.fecha = fecha;
    }

    public Anuncio getAnuncio() {
        return anuncio;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getOrigen() {
        return origen;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

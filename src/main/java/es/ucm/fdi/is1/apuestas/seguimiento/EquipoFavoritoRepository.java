package es.ucm.fdi.is1.apuestas.seguimiento;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio de {@link EquipoFavorito}. */
public interface EquipoFavoritoRepository extends JpaRepository<EquipoFavorito, Long> {

    Optional<EquipoFavorito> findByUsuarioAndEquipo(Usuario usuario, Equipo equipo);

    List<EquipoFavorito> findByUsuario(Usuario usuario);

    /** Los que siguen a alguno de esos equipos: los dos de un partido nuevo. */
    List<EquipoFavorito> findByEquipoIn(Collection<Equipo> equipos);
}

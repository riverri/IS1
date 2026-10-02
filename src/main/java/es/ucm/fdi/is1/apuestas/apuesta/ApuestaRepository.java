package es.ucm.fdi.is1.apuestas.apuesta;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

public interface ApuestaRepository extends JpaRepository<Apuesta, Long> {

    List<Apuesta> findByUsuarioAndEstadoOrderByFechaDesc(Usuario usuario, EstadoApuesta estado);

    List<Apuesta> findByUsuarioAndEstadoInOrderByFechaDesc(Usuario usuario, Collection<EstadoApuesta> estados);

    List<Apuesta> findByEstadoIn(Collection<EstadoApuesta> estados);

    long countByUsuarioAndEstado(Usuario usuario, EstadoApuesta estado);
}

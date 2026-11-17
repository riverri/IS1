package es.ucm.fdi.is1.apuestas.ligas;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio de {@link Reto}. */
public interface RetoRepository extends JpaRepository<Reto, Long> {

    List<Reto> findByEventoAndEstadoIn(Evento evento, Collection<EstadoReto> estados);

    boolean existsByEvento(Evento evento);

    @Query("select r from Reto r where r.retador = :usuario or r.retado = :usuario order by r.creado desc, r.id desc")
    List<Reto> deUsuario(Usuario usuario);
}

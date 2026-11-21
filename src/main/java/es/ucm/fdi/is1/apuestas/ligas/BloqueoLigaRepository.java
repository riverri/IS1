package es.ucm.fdi.is1.apuestas.ligas;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

public interface BloqueoLigaRepository extends JpaRepository<BloqueoLiga, Long> {
    boolean existsByLigaAndObjetivoAndEventoAndAlcance(Liga liga, Usuario objetivo, Evento evento, String alcance);
    boolean existsByLigaAndEventoAndAlcanceAndObjetivoIsNull(Liga liga, Evento evento, String alcance);
    List<BloqueoLiga> findByObjetivoAndEvento(Usuario objetivo, Evento evento);
    List<BloqueoLiga> findByEvento(Evento evento);
    List<BloqueoLiga> findByLiga(Liga liga);

    /** Solo bloqueos vigentes de la liga, con los datos que muestra la página ya cargados. */
    @Query("select distinct b from BloqueoLiga b join fetch b.evento e"
            + " join fetch e.local join fetch e.visitante left join fetch b.objetivo"
            + " where b.liga = :liga and e.estado = es.ucm.fdi.is1.apuestas.eventos.EstadoEvento.PROGRAMADO"
            + " and e.fechaHora > :ahora")
    List<BloqueoLiga> activosDeLiga(@Param("liga") Liga liga, @Param("ahora") LocalDateTime ahora);
}

package es.ucm.fdi.is1.apuestas.ligas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

public interface BloqueoLigaRepository extends JpaRepository<BloqueoLiga, Long> {
    boolean existsByLigaAndObjetivoAndEventoAndAlcance(Liga liga, Usuario objetivo, Evento evento, String alcance);
    List<BloqueoLiga> findByObjetivoAndEvento(Usuario objetivo, Evento evento);
}

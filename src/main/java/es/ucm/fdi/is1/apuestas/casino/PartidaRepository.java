package es.ucm.fdi.is1.apuestas.casino;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio Spring Data de las partidas del casino (HU-56 a HU-58). */
public interface PartidaRepository extends JpaRepository<Partida, Long> {

    /** La mano de blackjack que el usuario tiene a medias, si la tiene (CA-57.5). */
    Optional<Partida> findFirstByUsuarioAndJuegoAndEstado(Usuario usuario, TipoJuego juego, EstadoPartida estado);

    /** Historial (CA-58.1): las 50 últimas, de la más reciente a la más antigua. */
    List<Partida> findTop50ByUsuarioOrderByFechaDescIdDesc(Usuario usuario);

    /**
     * Para el panel de la casa (HU-54): por juego, [juego, partidas, apostado, pagado] de las partidas terminadas.
     */
    @Query("""
            select p.juego, count(p), coalesce(sum(p.apostado), 0), coalesce(sum(p.premio), 0) from Partida p
            where p.estado = es.ucm.fdi.is1.apuestas.casino.EstadoPartida.TERMINADA group by p.juego""")
    List<Object[]> resumenPorJuego();
}

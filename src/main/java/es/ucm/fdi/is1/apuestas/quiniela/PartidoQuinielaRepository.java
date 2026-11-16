package es.ucm.fdi.is1.apuestas.quiniela;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.eventos.Evento;

/** Repositorio de {@link PartidoQuiniela}. */
public interface PartidoQuinielaRepository extends JpaRepository<PartidoQuiniela, Long> {

    /** Si el evento está en alguna quiniela: entonces no se puede borrar ni cambiar de equipos. */
    boolean existsByEvento(Evento evento);
}

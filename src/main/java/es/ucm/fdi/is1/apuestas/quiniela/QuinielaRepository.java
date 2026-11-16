package es.ucm.fdi.is1.apuestas.quiniela;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de {@link Quiniela}. */
public interface QuinielaRepository extends JpaRepository<Quiniela, Long> {

    List<Quiniela> findAllByOrderByCreadaDesc();
}

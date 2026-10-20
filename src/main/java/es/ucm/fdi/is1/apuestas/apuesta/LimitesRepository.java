package es.ucm.fdi.is1.apuestas.apuesta;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de {@link Limites}. Solo hay una fila (id {@code Limites.ID}); se usa desde {@link LimitesService}. */
public interface LimitesRepository extends JpaRepository<Limites, Long> {
}

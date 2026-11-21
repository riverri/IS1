package es.ucm.fdi.is1.apuestas.casino;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de {@link ConfiguracionCasino}: solo hay una fila (id {@code ConfiguracionCasino.ID}). */
public interface ConfiguracionCasinoRepository extends JpaRepository<ConfiguracionCasino, Long> {
}

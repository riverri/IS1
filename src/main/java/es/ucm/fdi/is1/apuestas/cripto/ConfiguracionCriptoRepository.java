package es.ucm.fdi.is1.apuestas.cripto;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de la configuración de la sección de criptomonedas (una sola fila). */
public interface ConfiguracionCriptoRepository extends JpaRepository<ConfiguracionCripto, Long> {
}

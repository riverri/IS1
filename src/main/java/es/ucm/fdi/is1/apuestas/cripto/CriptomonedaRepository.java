package es.ucm.fdi.is1.apuestas.cripto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de las criptomonedas. */
public interface CriptomonedaRepository extends JpaRepository<Criptomoneda, Long> {

    Optional<Criptomoneda> findBySimbolo(String simbolo);

    List<Criptomoneda> findByActivaTrue();
}

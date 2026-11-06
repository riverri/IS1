package es.ucm.fdi.is1.apuestas.cripto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Repositorio Spring Data de las criptomonedas. */
public interface CriptomonedaRepository extends JpaRepository<Criptomoneda, Long> {

    Optional<Criptomoneda> findBySimbolo(String simbolo);

    List<Criptomoneda> findByActivaTrue();

    /** Las que necesitan precio: las que se ofrecen y las retiradas que alguien tiene todavía (CA-73.2). */
    @Query("select c from Criptomoneda c where c.activa = true"
            + " or exists (select p from PosicionCripto p where p.cripto = c and p.cantidad > 0)")
    List<Criptomoneda> conPrecio();
}

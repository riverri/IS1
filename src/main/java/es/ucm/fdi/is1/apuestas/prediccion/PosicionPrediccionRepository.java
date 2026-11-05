package es.ucm.fdi.is1.apuestas.prediccion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio Spring Data de las posiciones en los mercados de predicción. */
public interface PosicionPrediccionRepository extends JpaRepository<PosicionPrediccion, Long> {

    Optional<PosicionPrediccion> findByUsuarioAndMercadoAndLado(Usuario usuario, MercadoPrediccion mercado, Lado lado);

    List<PosicionPrediccion> findByMercado(MercadoPrediccion mercado);

    List<PosicionPrediccion> findByUsuarioAndMercado(Usuario usuario, MercadoPrediccion mercado);

    /** Posiciones de algunos usuarios en un mercado: las de los compañeros de liga (HU-68). */
    List<PosicionPrediccion> findByMercadoAndUsuarioIdIn(MercadoPrediccion mercado, Collection<Long> usuarios);
}

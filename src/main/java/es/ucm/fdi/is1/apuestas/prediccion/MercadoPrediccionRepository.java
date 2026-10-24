package es.ucm.fdi.is1.apuestas.prediccion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de los mercados de predicción. */
public interface MercadoPrediccionRepository extends JpaRepository<MercadoPrediccion, Long> {

    /** Todos, del que cierra antes al que cierra después. */
    List<MercadoPrediccion> findAllByOrderByCierreAsc();
}

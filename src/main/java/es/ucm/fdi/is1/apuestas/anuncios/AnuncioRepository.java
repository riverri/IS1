package es.ucm.fdi.is1.apuestas.anuncios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Repositorio Spring Data de los anuncios (HU-60, HU-61). */
public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {

    /** Los que están activos; las fechas y la sección se comprueban después con {@link Anuncio#seMuestra}. */
    List<Anuncio> findByActivoTrue();

    /** Todos, para Gestión: primero los activos y, dentro, los de más prioridad. */
    List<Anuncio> findAllByOrderByActivoDescPrioridadDescTituloAsc();

    /** Suma en bloque impresiones acumuladas en memoria, sin leer y guardar la entidad. */
    @Modifying
    @Query("update Anuncio a set a.impresiones = a.impresiones + :cantidad where a.id = :id")
    int sumarImpresiones(Long id, long cantidad);
}

package es.ucm.fdi.is1.apuestas.anuncios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Repositorio Spring Data de los clics en anuncios (HU-62). */
public interface ClicAnuncioRepository extends JpaRepository<ClicAnuncio, Long> {

    /** Clics de cada anuncio: filas [id del anuncio, número de clics]. Los que no tienen clics no salen. */
    @Query("select c.anuncio.id, count(c) from ClicAnuncio c group by c.anuncio.id")
    List<Object[]> clicsPorAnuncio();

    long countByAnuncio(Anuncio anuncio);
}

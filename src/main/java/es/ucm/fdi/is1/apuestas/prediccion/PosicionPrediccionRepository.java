package es.ucm.fdi.is1.apuestas.prediccion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio Spring Data de las posiciones en los mercados de predicción. */
public interface PosicionPrediccionRepository extends JpaRepository<PosicionPrediccion, Long> {

    Optional<PosicionPrediccion> findByUsuarioAndMercadoAndLado(Usuario usuario, MercadoPrediccion mercado, Lado lado);

    List<PosicionPrediccion> findByMercado(MercadoPrediccion mercado);

    /**
     * Para el panel de la casa: lo devuelto al anular los mercados en ese estado. Se devuelve lo invertido, y solo si es
     * positivo y no a cuentas eliminadas (ver {@code PrediccionService.anular}).
     */
    @Query("select coalesce(sum(p.invertido), 0) from PosicionPrediccion p"
            + " where p.mercado.estado = :estado and p.invertido > 0 and p.usuario.eliminado = false")
    BigDecimal devuelto(EstadoPrediccion estado);

    List<PosicionPrediccion> findByUsuarioAndMercado(Usuario usuario, MercadoPrediccion mercado);

    /** Posiciones de algunos usuarios en un mercado: las de los compañeros de liga (HU-68). */
    List<PosicionPrediccion> findByMercadoAndUsuarioIdIn(MercadoPrediccion mercado, Collection<Long> usuarios);

    /** Posiciones en mercados resueltos, con su mercado y su usuario: ranking por tipo de juego (HU-84). */
    @Query("select p from PosicionPrediccion p join fetch p.mercado m join fetch p.usuario"
            + " where m.estado = es.ucm.fdi.is1.apuestas.prediccion.EstadoPrediccion.RESUELTO")
    List<PosicionPrediccion> deMercadosResueltos();

    /** Totales para el ranking sin cargar cada posición ni sus entidades relacionadas en memoria. */
    @Query("select p.usuario.id, count(p), sum(case when p.lado = m.resultado then 1 else 0 end),"
            + " sum(p.invertido), sum(p.pagado) from PosicionPrediccion p join p.mercado m"
            + " where m.estado = es.ucm.fdi.is1.apuestas.prediccion.EstadoPrediccion.RESUELTO"
            + " group by p.usuario.id")
    List<Object[]> resumenResueltasPorUsuario();
}

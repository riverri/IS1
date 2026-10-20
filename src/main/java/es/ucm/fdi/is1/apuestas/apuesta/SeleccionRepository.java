package es.ucm.fdi.is1.apuestas.apuesta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;

/**
 * Repositorio de {@link Seleccion}. Se busca por selección y no por apuesta porque al resolver un evento
 * o un mercado hay que encontrar todas las apuestas que lo incluyen, sean simples o combinadas.
 */
public interface SeleccionRepository extends JpaRepository<Seleccion, Long> {

    /** Todas las selecciones de un evento, de apuestas en cualquier estado. */
    List<Seleccion> findByEvento(Evento evento);

    /** Si alguien ha apostado alguna vez al evento: entonces no se puede borrar ni cambiar de equipos (HU-46). */
    boolean existsByEvento(Evento evento);

    /** Selecciones a cualquier candidato del mercado (navega {@code candidato.mercado}). */
    List<Seleccion> findByCandidatoMercado(Mercado mercado);

    /**
     * Por cada selección 1X2 de una apuesta activa al evento: [pronóstico, id del usuario, importe de la apuesta,
     * número de selecciones de la apuesta], para el ajuste por volumen.
     */
    @Query("""
            select s.pronostico, s.apuesta.usuario.id, s.apuesta.importe, size(s.apuesta.selecciones)
            from Seleccion s
            where s.evento = :evento and s.pronostico is not null
              and s.apuesta.estado = es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.ACTIVA""")
    List<Object[]> seleccionesActivasPorPronostico(Evento evento);
}

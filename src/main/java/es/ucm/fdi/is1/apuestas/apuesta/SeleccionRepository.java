package es.ucm.fdi.is1.apuestas.apuesta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;

public interface SeleccionRepository extends JpaRepository<Seleccion, Long> {

    List<Seleccion> findByEvento(Evento evento);

    boolean existsByEvento(Evento evento);

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
